package com.example.home_service_backend.agent.model;

import com.example.home_service_backend.agent.config.AgentModelProperties;
import com.example.home_service_backend.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** OpenAI 兼容 Embeddings 客户端，可独立于 Chat 模型配置供应商和地址。 */
public final class OpenAiCompatibleEmbeddingClient implements EmbeddingClient {
    private final AgentModelProperties properties;
    private final RestClient client;

    public OpenAiCompatibleEmbeddingClient(AgentModelProperties properties) {
        this.properties = properties;
        this.client = createClient();
    }

    @Override
    public List<Double> embed(String text) {
        String apiKey = properties.resolvedEmbeddingApiKey();
        if (!properties.enabled()
                || apiKey == null || apiKey.isBlank()
                || properties.embeddingModel() == null || properties.embeddingModel().isBlank()) {
            throw new BusinessException("503", "Embedding 模型尚未配置");
        }
        try {
            JsonNode root = client.post()
                    .uri(properties.embeddingPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of("model", properties.embeddingModel(), "input", text))
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode embedding = root == null ? null : root.path("data").path(0).path("embedding");
            if (embedding == null || !embedding.isArray()) {
                throw new BusinessException("502", "Embedding 返回为空");
            }
            List<Double> vector = new ArrayList<>();
            embedding.forEach(value -> vector.add(value.asDouble()));
            return vector;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("502", "Embedding 服务暂时不可用");
        }
    }

    private RestClient createClient() {
        String baseUrl = properties.resolvedEmbeddingBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException("503", "Embedding 服务地址尚未配置");
        }
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.timeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
