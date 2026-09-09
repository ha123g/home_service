package com.example.home_service_backend.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.ai.memory")
/**
 * Agent 内存属性
 */
public record AgentMemoryProperties(
        String mode,
        Duration ttl,
        String redisKeyPrefix) {
    public AgentMemoryProperties {
        mode = mode == null || mode.isBlank() ? "SUMMARY_CACHE" : mode;
        ttl = ttl == null || ttl.isZero() || ttl.isNegative() ? Duration.ofDays(1) : ttl;
        redisKeyPrefix = redisKeyPrefix == null || redisKeyPrefix.isBlank()
                ? "home-service:ai:summary:v1:" : redisKeyPrefix;
    }
}
