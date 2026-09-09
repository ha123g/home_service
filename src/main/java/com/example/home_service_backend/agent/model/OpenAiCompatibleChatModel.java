package com.example.home_service_backend.agent.model;

import com.example.home_service_backend.agent.config.AgentModelProperties;
import com.example.home_service_backend.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 调用 DashScope、Ollama 等供应商提供的 OpenAI 兼容 Chat Completions 协议。 */
public final class OpenAiCompatibleChatModel implements ChatModelClient {
    private final AgentModelProperties properties;
    private final String model;
    private final double temperature;
    private final int configuredMaxOutputTokens;
    private final RestClient client;

    public OpenAiCompatibleChatModel(
            AgentModelProperties properties,
            String model,
            double temperature,
            int configuredMaxOutputTokens) {
        this.properties = properties;
        this.model = model;
        this.temperature = temperature;
        this.configuredMaxOutputTokens = configuredMaxOutputTokens;
        this.client = createClient(properties.resolvedChatBaseUrl());
    }

    @Override
    public ChatResult chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        long start = System.nanoTime();
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", temperature,
                "max_tokens", Math.min(maxOutputTokens, configuredMaxOutputTokens),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)));
        JsonNode root = execute(body);
        try {
            String content = root == null
                    ? null
                    : root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new BusinessException("502", "大模型返回为空");
            }
            int input = root.path("usage").path("prompt_tokens").asInt(0);
            int output = root.path("usage").path("completion_tokens").asInt(0);
            return new ChatResult(content, input, output, (System.nanoTime() - start) / 1_000_000);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException("502", "大模型响应解析失败");
        }
    }

    @Override
    public ToolCallResult callTools(String systemPrompt, String userPrompt,
                                    List<ToolDefinition> tools, int maxOutputTokens) {
        if (tools == null || tools.isEmpty()) {
            throw new BusinessException("500", "Function Calling 未配置可用工具");
        }
        long start = System.nanoTime();
        List<Map<String, Object>> definitions = tools.stream().map(tool -> Map.<String, Object>of(
                "type", "function",
                "function", Map.of(
                        "name", tool.name(),
                        "description", tool.description(),
                        "parameters", tool.parameters())))
                .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", temperature);
        body.put("max_tokens", Math.min(maxOutputTokens, configuredMaxOutputTokens));
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));
        body.put("tools", definitions);
        body.put("tool_choice", "auto");
        body.put("parallel_tool_calls", true);
        JsonNode root = execute(body);
        try {
            JsonNode message = root.path("choices").path(0).path("message");
            List<ToolCall> calls = new ArrayList<>();
            JsonNode toolCalls = message.path("tool_calls");
            if (toolCalls.isArray()) {
                for (JsonNode item : toolCalls) {
                    JsonNode function = item.path("function");
                    String name = function.path("name").asText(null);
                    if (name == null || name.isBlank()) continue;
                    JsonNode arguments = function.path("arguments");
                    String argumentsJson = arguments.isTextual() ? arguments.asText() : arguments.toString();
                    calls.add(new ToolCall(item.path("id").asText(null), name, argumentsJson));
                }
            }
            String content = message.path("content").asText(null);
            if (calls.isEmpty() && (content == null || content.isBlank())) {
                throw new BusinessException("502", "大模型未选择业务工具");
            }
            return new ToolCallResult(content, calls,
                    root.path("usage").path("prompt_tokens").asInt(0),
                    root.path("usage").path("completion_tokens").asInt(0),
                    (System.nanoTime() - start) / 1_000_000);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException("502", "Function Calling 响应解析失败");
        }
    }

    private JsonNode execute(Map<String, Object> body) {
        String apiKey = properties.resolvedChatApiKey();
        if (!properties.enabled() || apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("503", "大模型尚未配置");
        }
        try {
            JsonNode root = client.post()
                    .uri(properties.chatPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (root == null || root.path("choices").isMissingNode() || root.path("choices").isEmpty()) {
                throw new BusinessException("502", "大模型返回为空");
            }
            return root;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("502", "大模型服务暂时不可用", exception);
        }
    }

    private RestClient createClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException("503", "大模型服务地址尚未配置");
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
