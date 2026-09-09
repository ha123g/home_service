package com.example.home_service_backend.agent.rag;

import com.example.home_service_backend.agent.config.AgentRagProperties;
import com.example.home_service_backend.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Milvus REST v2 只读检索适配器；表达式值仅来自后端受控分类/租户。 */
@Component
public class MilvusVectorSearchClient {
    private static final List<String> OUTPUT_FIELDS = List.of(
            "document_id", "chunk_id", "source", "title", "content", "tenant_id",
            "category", "version", "status", "effective_from", "effective_to");

    private final AgentRagProperties properties;
    private final RestClient client;

    public MilvusVectorSearchClient(AgentRagProperties properties) {
        this.properties = properties;
        this.client = createClient();
    }

    public List<RagDocument> search(List<Double> vector, String category, String tenantId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("collectionName", properties.collection());
        body.put("data", List.of(vector));
        body.put("limit", properties.topK());
        body.put("outputFields", OUTPUT_FIELDS);
        String filter = buildFilter(category, tenantId);
        body.put("filter", filter);
        try {
            JsonNode root = client.post()
                    .uri(properties.searchPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            List<RagDocument> documents = new ArrayList<>();
            if (root != null && root.path("data").isArray()) {
                root.path("data").forEach(node -> documents.add(toDocument(node)));
            }
            return documents;
        } catch (Exception exception) {
            throw new BusinessException("502", "Milvus 检索暂时不可用");
        }
    }

    private RagDocument toDocument(JsonNode node) {
        return new RagDocument(
                text(node, "document_id"),
                text(node, "chunk_id"),
                text(node, "source"),
                text(node, "title"),
                text(node, "content"),
                text(node, "tenant_id"),
                text(node, "category"),
                text(node, "version"),
                text(node, "status"),
                text(node, "effective_from"),
                text(node, "effective_to"));
    }

    private String text(JsonNode node, String field) {
        return node.path(field).isMissingNode() || node.path(field).isNull()
                ? null
                : node.path(field).asText();
    }

    private String buildFilter(String category, String tenantId) {
        List<String> conditions = new ArrayList<>();
        conditions.add("status == \"ACTIVE\"");
        if (category != null && !category.isBlank()) {
            conditions.add("category == \"" + escapeFilterValue(category) + "\"");
        }
        if (tenantId != null && !tenantId.isBlank()) {
            conditions.add("tenant_id == \"" + escapeFilterValue(tenantId) + "\"");
        }
        return String.join(" and ", conditions);
    }

    private String escapeFilterValue(String value) {
        if (!value.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new BusinessException("400", "RAG 元数据过滤值不合法");
        }
        return value;
    }

    private RestClient createClient() {
        if (!properties.enabled()
                || properties.milvusBaseUrl() == null
                || properties.milvusBaseUrl().isBlank()) {
            return RestClient.builder().baseUrl("http://localhost").build();
        }
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.timeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        return RestClient.builder()
                .baseUrl(properties.milvusBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
