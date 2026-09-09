package com.example.home_service_backend.vo.rag;
import java.time.LocalDateTime;
public record KnowledgeDocumentView(Long id, String documentKey, String title, String source,
        String category, String tenantId, Integer currentVersion, String status,
        String versionStatus, Integer chunkCount, LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo, String indexError, LocalDateTime updatedTime) {}
