package com.example.home_service_backend.agent.service;

import com.example.home_service_backend.agent.config.AgentPersistenceProperties;
import com.example.home_service_backend.entity.AiRequestLog;
import com.example.home_service_backend.repository.AiRequestLogRepository;
import com.example.home_service_backend.repository.AiLogCleanupRepository;
import com.example.home_service_backend.repository.AiUsageDailyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AI 可观测性：只持久化采样元数据，并在内存按低基数维度聚合每日用量。
 * 此服务从不接收用户消息、Prompt、回答或 Tool payload。
 */
@Service
public class AgentAuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentAuditService.class);
    private static final String UNKNOWN_MODEL = "rule-or-unconfigured";

    private final AiRequestLogRepository requestLogRepository;
    private final AiUsageDailyRepository usageDailyRepository;
    private final AiLogCleanupRepository cleanupRepository;
    private final AgentPersistenceProperties properties;
    private Map<UsageKey, UsageDelta> usageBuffer = new HashMap<>();

    public AgentAuditService(
            AiRequestLogRepository requestLogRepository,
            AiUsageDailyRepository usageDailyRepository,
            AiLogCleanupRepository cleanupRepository,
            AgentPersistenceProperties properties) {
        this.requestLogRepository = requestLogRepository;
        this.usageDailyRepository = usageDailyRepository;
        this.cleanupRepository = cleanupRepository;
        this.properties = properties;
    }

    /**
     * 记录成功请求。
     */
    public void recordSuccess(
            String requestId,
            Long userId,
            String route,
            String componentType,
            String modelName,
            int inputTokens,
            int outputTokens,
            int retrievalCount,
            long latencyMs) {
        recordUsage(route, modelName, true, inputTokens, outputTokens, retrievalCount, latencyMs);
        boolean full = "BUSINESS".equals(route);
        if (!full && !sampleRead()) {
            return;
        }
        saveRequestLog(requestId, userId, route, full ? "FULL" : "SAMPLED", componentType,
                modelName, inputTokens, outputTokens, retrievalCount, latencyMs, "SUCCESS", null);
    }

    /**
     * 记录失败请求。
     */
    public void recordFailure(
            String requestId,
            Long userId,
            String route,
            String modelName,
            long latencyMs,
            String errorCode) {
        recordUsage(route, modelName, false, 0, 0, 0, latencyMs);
        saveRequestLog(requestId, userId, route, "ERROR", null, modelName,
                0, 0, 0, latencyMs, "FAILED", limit(errorCode, 64));
    }
    /**
     * 保存请求日志。
     */

    private void saveRequestLog(
            String requestId,
            Long userId,
            String route,
            String detailLevel,
            String componentType,
            String modelName,
            int inputTokens,
            int outputTokens,
            int retrievalCount,
            long latencyMs,
            String status,
            String errorCode) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        AiRequestLog log = new AiRequestLog();
        log.setRequestId(requestId);
        log.setUserId(userId);
        log.setRouteType(normalize(route, "UNKNOWN"));
        log.setDetailLevel(detailLevel);
        log.setAgentName(normalize(route, "UNKNOWN") + "_AGENT");
        log.setComponentType(limit(componentType, 64));
        log.setModelName(limit(normalize(modelName, UNKNOWN_MODEL), 128));
        log.setInputTokens(inputTokens);
        log.setOutputTokens(outputTokens);
        log.setRetrievalCount(retrievalCount);
        log.setToolCallCount(0);
        log.setLatencyMs((int) Math.min(Integer.MAX_VALUE, Math.max(0L, latencyMs)));
        log.setStatus(status);
        log.setErrorCode(errorCode);
        log.setCreatedTime(now);
        log.setExpireTime(now.plus(requestRetention()));
        try {
            requestLogRepository.save(log);
        } catch (DataAccessException exception) {
            LOGGER.warn("AI 请求元数据保存失败；请求编号={}", requestId);
        }
    }
    /**
     * 记录使用情况。
     */

    private synchronized void recordUsage(
            String route,
            String modelName,
            boolean success,
            int inputTokens,
            int outputTokens,
            int retrievalCount,
            long latencyMs) {
        UsageKey key = new UsageKey(
                LocalDate.now(ZoneOffset.UTC),
                normalize(route, "UNKNOWN"),
                normalize(route, "UNKNOWN") + "_AGENT",
                normalize(modelName, UNKNOWN_MODEL));
        UsageDelta delta = usageBuffer.computeIfAbsent(key, ignored -> new UsageDelta());
        delta.add(success, inputTokens, outputTokens, retrievalCount, latencyMs);
    }

    /**
     * 定时刷新使用情况。
     */
    @Scheduled(fixedDelayString = "${app.ai.persistence.usage-flush-interval:5m}")
    public void flushUsage() {
        Map<UsageKey, UsageDelta> batch;
        synchronized (this) {
            if (usageBuffer.isEmpty()) {
                return;
            }
            batch = usageBuffer;
            usageBuffer = new HashMap<>();
        }
        Map<UsageKey, UsageDelta> failed = new HashMap<>();
        batch.forEach((key, delta) -> {
            try {
                usageDailyRepository.addDelta(
                        key.date(), key.route(), key.agent(), key.model(),
                        delta.requests, delta.successes, delta.failures,
                        delta.inputTokens, delta.outputTokens, 0,
                        delta.retrievalCount, delta.totalLatencyMs);
            } catch (DataAccessException exception) {
                failed.put(key, delta);
            }
        });
        if (!failed.isEmpty()) {
            synchronized (this) {
                failed.forEach((key, delta) ->
                        usageBuffer.merge(key, delta, UsageDelta::merge));
            }
            LOGGER.warn("AI 每日用量部分刷新失败；失败维度数={}", failed.size());
        }
    }

    /**
     * 定时清理过期日志。
     */
    @Scheduled(fixedDelayString = "${app.ai.persistence.cleanup-interval:1h}")
    public void cleanupExpiredDetails() {
        int batchSize = Math.max(1, Math.min(5000, properties.cleanupBatchSize()));
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        try {
            cleanupRepository.deleteExpiredRequestLogs(now, batchSize);
            cleanupRepository.deleteExpiredToolLogs(now, batchSize);
        } catch (DataAccessException exception) {
            LOGGER.warn("过期 AI 元数据清理失败");
        }
    }

    /**
     * 随机采样读取请求。
     */
    private boolean sampleRead() {
        double rate = Math.max(0D, Math.min(1D, properties.readSampleRate()));
        return ThreadLocalRandom.current().nextDouble() < rate;
    }

    /**
     * 获取请求日志保留时长。
     */
    private Duration requestRetention() {
        Duration value = properties.requestLogRetention();
        return value == null || value.isNegative() || value.isZero() ? Duration.ofDays(30) : value;
    }

    /**
     * 规范化字符串。
     */
    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * 截断字符串。
     */
    private String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * 使用日期、路由、代理和模型唯一标识一次使用。
     */
    private record UsageKey(LocalDate date, String route, String agent, String model) {
    }

    /**
     * 聚合使用情况。
     */
    private static final class UsageDelta {
        private long requests;
        private long successes;
        private long failures;
        private long inputTokens;
        private long outputTokens;
        private long retrievalCount;
        private long totalLatencyMs;

        /**
         * 添加使用情况。
         */
        private void add(boolean success, int input, int output, int retrievals, long latency) {
            requests++;
            successes += success ? 1 : 0;
            failures += success ? 0 : 1;
            inputTokens += Math.max(0, input);
            outputTokens += Math.max(0, output);
            retrievalCount += Math.max(0, retrievals);
            totalLatencyMs += Math.max(0L, latency);
        }

        /**
         * 合并使用情况。
         */
        private static UsageDelta merge(UsageDelta left, UsageDelta right) {
            left.requests += right.requests;
            left.successes += right.successes;
            left.failures += right.failures;
            left.inputTokens += right.inputTokens;
            left.outputTokens += right.outputTokens;
            left.retrievalCount += right.retrievalCount;
            left.totalLatencyMs += right.totalLatencyMs;
            return left;
        }
    }
}
