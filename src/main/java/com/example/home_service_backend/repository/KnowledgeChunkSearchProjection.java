package com.example.home_service_backend.repository;

import java.time.LocalDateTime;

/** 仅返回知识检索所需字段，避免把 Entity 暴露到 Agent 层。 */
public interface KnowledgeChunkSearchProjection {
    String getDocumentId();
    String getChunkId();
    String getSource();
    String getTitle();
    String getContent();
    String getTenantId();
    String getCategory();
    Integer getVersion();
    String getStatus();
    LocalDateTime getEffectiveFrom();
    LocalDateTime getEffectiveTo();
}
