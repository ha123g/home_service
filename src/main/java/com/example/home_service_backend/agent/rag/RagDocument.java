package com.example.home_service_backend.agent.rag;

/** 可追溯的知识块元数据；content 只用于本次检索上下文，不写入 AI 日志。 */
public record RagDocument(
        String documentId,
        String chunkId,
        String source,
        String title,
        String content,
        String tenantId,
        String category,
        String version,
        String status,
        String effectiveFrom,
        String effectiveTo) {
}
