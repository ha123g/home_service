package com.example.home_service_backend.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.agent.rag")
/**
 * Agent RAG属性
 */

public record AgentRagProperties(boolean enabled, String milvusBaseUrl, String collection,
                                 String hasCollectionPath, String createCollectionPath,
                                 String searchPath, String insertPath, String deletePath,
                                 int topK, int maxContextChunks,
                                 Duration timeout) {
    public AgentRagProperties {
        milvusBaseUrl = defaultValue(milvusBaseUrl, "http://localhost:19530");
        collection = defaultValue(collection, "home_service_knowledge");
        hasCollectionPath = defaultValue(hasCollectionPath, "/v2/vectordb/collections/has");
        createCollectionPath = defaultValue(createCollectionPath, "/v2/vectordb/collections/create");
        searchPath = defaultValue(searchPath, "/v2/vectordb/entities/search");
        insertPath = defaultValue(insertPath, "/v2/vectordb/entities/insert");
        deletePath = defaultValue(deletePath, "/v2/vectordb/entities/delete");
        topK = topK <= 0 ? 8 : Math.min(topK, 50);
        maxContextChunks = maxContextChunks <= 0 ? 6 : Math.min(maxContextChunks, 20);
        timeout = timeout == null || timeout.isZero() || timeout.isNegative()
                ? Duration.ofSeconds(5) : timeout;
    }
    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
