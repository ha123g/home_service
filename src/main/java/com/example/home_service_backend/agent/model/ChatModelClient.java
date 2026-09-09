package com.example.home_service_backend.agent.model;

import java.util.List;
import java.util.Map;

public interface ChatModelClient {
    ChatResult chat(String systemPrompt, String userPrompt, int maxOutputTokens);

    /**
     * 使用 OpenAI 兼容协议的 Function Calling。模型只负责选择受控工具并提供 JSON 参数，
     * 工具的权限校验和实际执行仍由应用层完成。
     */
    ToolCallResult callTools(String systemPrompt, String userPrompt,
                             List<ToolDefinition> tools, int maxOutputTokens);

    record ChatResult(String content, int inputTokens, int outputTokens, long latencyMs) {}

    record ToolDefinition(String name, String description, Map<String, Object> parameters) {
        public ToolDefinition {
            parameters = parameters == null ? Map.of("type", "object", "properties", Map.of()) : Map.copyOf(parameters);
        }

        public static ToolDefinition withoutArguments(String name, String description) {
            return new ToolDefinition(name, description,
                    Map.of("type", "object", "properties", Map.of(), "additionalProperties", false));
        }
    }

    record ToolCall(String id, String name, String argumentsJson) {}

    record ToolCallResult(String content, List<ToolCall> toolCalls,
                          int inputTokens, int outputTokens, long latencyMs) {
        public ToolCallResult {
            toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        }
    }
}
