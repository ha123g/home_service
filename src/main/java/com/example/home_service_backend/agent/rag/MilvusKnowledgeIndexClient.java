package com.example.home_service_backend.agent.rag;

import com.example.home_service_backend.agent.config.AgentRagProperties;
import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.*;

/** Milvus REST v2 写入/删除适配器；MySQL 记录事实，Milvus 只保存可重建索引。 */
@Component
public class MilvusKnowledgeIndexClient {
    private final AgentRagProperties properties;
    private final AgentModelFactory modelFactory;
    private final RestClient client;
    public MilvusKnowledgeIndexClient(AgentRagProperties properties, AgentModelFactory modelFactory) {
        this.properties = properties; this.modelFactory = modelFactory; this.client = createClient();
    }
    public void index(KnowledgeDocumentIndex index) {
        if (!properties.enabled()) {
            throw new BusinessException("503", "RAG 向量索引未启用");
        }
        List<Map<String,Object>> data = new ArrayList<>();
        for (KnowledgeChunkIndex chunk : index.chunks()) {
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("document_id", index.documentId()); row.put("chunk_id", chunk.chunkId());
            row.put("source", index.source()); row.put("title", index.title()); row.put("content", chunk.content());
            // Milvus 标量字段通常默认不可为 NULL；无租户文档使用空字符串表示平台公共知识。
            row.put("tenant_id", valueOrEmpty(index.tenantId())); row.put("category", index.category());
            row.put("version", String.valueOf(index.version())); row.put("status", "ACTIVE");
            row.put("effective_from", valueOrEmpty(format(index.effectiveFrom()))); row.put("effective_to", valueOrEmpty(format(index.effectiveTo())));
            row.put("vector", modelFactory.createEmbedding().embed(chunk.content()));
            data.add(row);
        }
        try { client.post().uri(properties.insertPath()).contentType(MediaType.APPLICATION_JSON)
                .body(new LinkedHashMap<>(Map.of("collectionName", properties.collection(), "data", data))).retrieve().body(JsonNode.class); }
        catch (Exception e) { throw new BusinessException("502", "RAG 向量索引写入失败"); }
    }
    public void delete(String documentId) {
        if (!properties.enabled()) return;
        String safe = documentId.replace("\\", "\\\\").replace("\"", "\\\"");
        Map<String,Object> body = new LinkedHashMap<>(); body.put("collectionName", properties.collection()); body.put("filter", "document_id == \"" + safe + "\"");
        try { client.post().uri(properties.deletePath()).contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve().body(JsonNode.class); }
        catch (Exception e) { throw new BusinessException("502", "RAG 向量索引删除失败"); }
    }
    private String format(LocalDateTime value) { return value == null ? null : value.toString(); }
    private String valueOrEmpty(String value) { return value == null ? "" : value; }
    private RestClient createClient() {
        HttpClient http = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http); factory.setReadTimeout(properties.timeout());
        return RestClient.builder().baseUrl(properties.milvusBaseUrl()).requestFactory(factory).build();
    }
    public record KnowledgeDocumentIndex(String documentId, String title, String source, String category,
                                         String tenantId, int version, LocalDateTime effectiveFrom,
                                         LocalDateTime effectiveTo, List<KnowledgeChunkIndex> chunks) {}
    public record KnowledgeChunkIndex(String chunkId, String content) {}
}
