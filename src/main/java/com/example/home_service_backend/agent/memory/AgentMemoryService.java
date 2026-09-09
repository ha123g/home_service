package com.example.home_service_backend.agent.memory;

import com.example.home_service_backend.agent.config.AgentMemoryProperties;
import com.example.home_service_backend.common.constants.RedisConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Redis 短期摘要记忆；不保存完整消息正文。 */
@Service
public class AgentMemoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentMemoryService.class);
    private static final int MAX_SUMMARY_LENGTH = 4000;

    private final RedissonClient redis;
    private final AgentMemoryProperties properties;

    public AgentMemoryService(RedissonClient redis, AgentMemoryProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public String get(String userId, String sessionId) {
        try {
            return bucket(userId, sessionId).get();
        } catch (RuntimeException exception) {
            LOGGER.warn("AI 短期摘要读取失败，将不使用历史摘要继续处理");
            return null;
        }
    }

    public void putSummary(String userId, String sessionId, String summary) {
        if (summary == null || summary.isBlank()) {
            return;
        }
        String value = summary.length() > MAX_SUMMARY_LENGTH
                ? summary.substring(0, MAX_SUMMARY_LENGTH)
                : summary;
        try {
            bucket(userId, sessionId).set(value, ttl());
        } catch (RuntimeException exception) {
            LOGGER.warn("AI 短期摘要写入失败，本次响应仍可正常返回");
        }
    }

    private Duration ttl() {
        return properties.ttl() == null || properties.ttl().isNegative() || properties.ttl().isZero()
                ? Duration.ofDays(1)
                : properties.ttl();
    }

    private String key(String userId, String sessionId) {
        String prefix = properties.redisKeyPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = RedisConstants.AI_SUMMARY_NAMESPACE;
        }
        return prefix + userId + ":" + sessionId;
    }

    private RBucket<String> bucket(String userId, String sessionId) {
        return redis.getBucket(key(userId, sessionId));
    }
}
