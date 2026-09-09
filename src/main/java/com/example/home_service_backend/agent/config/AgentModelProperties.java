package com.example.home_service_backend.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.agent.model")
/**
 * Agent 模型属性
 */

public record AgentModelProperties(
        boolean enabled,
        String provider,
        String baseUrl,
        String chatBaseUrl,
        String embeddingBaseUrl,
        String apiKey,
        String chatApiKey,
        String embeddingApiKey,
        String chatPath,
        String embeddingPath,
        String embeddingModel,
        Duration timeout) {

    public AgentModelProperties {
        provider = provider == null || provider.isBlank() ? "dashscope" : provider;
        baseUrl = defaultValue(baseUrl, "https://dashscope.aliyuncs.com/compatible-mode/v1");
        chatBaseUrl = defaultValue(chatBaseUrl, baseUrl);
        embeddingBaseUrl = defaultValue(embeddingBaseUrl, "http://localhost:11434/v1");
        chatPath = defaultValue(chatPath, "/chat/completions");
        embeddingPath = defaultValue(embeddingPath, "/embeddings");
        embeddingModel = defaultValue(embeddingModel, "bge-m3");
        timeout = timeout == null || timeout.isZero() || timeout.isNegative()
                ? Duration.ofSeconds(30) : timeout;
    }

    public String resolvedChatBaseUrl() {
        return firstNonBlank(chatBaseUrl, baseUrl);
    }

    public String resolvedEmbeddingBaseUrl() {
        return firstNonBlank(embeddingBaseUrl, baseUrl);
    }

    public String resolvedChatApiKey() {
        return firstNonBlank(chatApiKey, apiKey);
    }

    public String resolvedEmbeddingApiKey() {
        return firstNonBlank(embeddingApiKey, apiKey);
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
