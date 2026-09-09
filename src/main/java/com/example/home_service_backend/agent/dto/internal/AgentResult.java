package com.example.home_service_backend.agent.dto.internal;

import com.example.home_service_backend.agent.rag.RagDocument;

import java.util.List;

/** 专用 Agent 内部结果，经过应用服务映射后才形成 API View。 */
public record AgentResult(
        String answer,
        String componentType,
        String schemaVersion,
        Object data,
        // 引用的文档
        List<RagDocument> citations,
        String route,
        int inputTokens,
        int outputTokens,
        long latencyMs) {
}
