package com.example.home_service_backend.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.ai.persistence")
/**
 * Agent 持久化属性
 */
public record AgentPersistenceProperties(
        Duration requestLogRetention,
        Duration toolLogRetention,
        double readSampleRate,
        Duration usageFlushInterval,
        int usageBufferMaxKeys,
        int cleanupBatchSize,
        Duration cleanupInterval,
        boolean storeConversationContent,
        boolean storeToolPayload) {
    public AgentPersistenceProperties {
        requestLogRetention = valid(requestLogRetention, Duration.ofDays(30));
        toolLogRetention = valid(toolLogRetention, Duration.ofDays(90));
        readSampleRate = Math.max(0D, Math.min(1D, readSampleRate == 0D ? 0.01D : readSampleRate));
        usageFlushInterval = valid(usageFlushInterval, Duration.ofMinutes(5));
        usageBufferMaxKeys = usageBufferMaxKeys <= 0 ? 1000 : usageBufferMaxKeys;
        cleanupBatchSize = cleanupBatchSize <= 0 ? 500 : cleanupBatchSize;
        cleanupInterval = valid(cleanupInterval, Duration.ofHours(1));
        // These safeguards are intentionally not configurable.
        storeConversationContent = false;
        storeToolPayload = false;
    }
    private static Duration valid(Duration value, Duration fallback) {
        return value == null || value.isZero() || value.isNegative() ? fallback : value;
    }
}
