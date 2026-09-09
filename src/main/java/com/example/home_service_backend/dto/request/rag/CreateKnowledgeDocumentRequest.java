package com.example.home_service_backend.dto.request.rag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
public record CreateKnowledgeDocumentRequest(
        @NotBlank @Size(max=128) @Pattern(regexp="[A-Za-z0-9][A-Za-z0-9_-]{0,127}") String documentKey,
        @NotBlank @Size(max=255) String title,
        @NotBlank @Size(max=512) String source,
        @NotBlank @Size(max=64) String category,
        @Size(max=64) String tenantId,
        @NotBlank @Size(max=200000) String content,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo) {}
