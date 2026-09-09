package com.example.home_service_backend.agent;

import com.example.home_service_backend.agent.rag.MetadataFilterModule;
import com.example.home_service_backend.agent.rag.RagDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataFilterModuleTests {
    private final MetadataFilterModule filter = new MetadataFilterModule();

    @Test
    void keepsOnlyActiveLatestEffectiveVersion() {
        RagDocument oldVersion = document("1", "1", "v1", "ACTIVE", "2020-01-01", null);
        RagDocument latest = document("1", "2", "v2", "ACTIVE", "2020-01-01", null);
        RagDocument expired = document("2", "3", "v1", "ACTIVE", "2020-01-01", "2021-01-01");
        RagDocument disabled = document("3", "4", "v1", "DISABLED", "2020-01-01", null);

        List<RagDocument> result = filter.filter(
                List.of(oldVersion, latest, expired, disabled), "knowledge", null);

        assertThat(result).containsExactly(latest);
    }

    @Test
    void rejectsMalformedEffectiveDate() {
        RagDocument malformed = document("1", "1", "v1", "ACTIVE", "not-a-date", null);

        assertThat(filter.filter(List.of(malformed), "knowledge", null)).isEmpty();
    }

    private RagDocument document(
            String documentId,
            String chunkId,
            String version,
            String status,
            String effectiveFrom,
            String effectiveTo) {
        return new RagDocument(documentId, chunkId, "admin", "title", "content",
                "platform", "knowledge", version, status, effectiveFrom, effectiveTo);
    }
}
