package com.example.home_service_backend.agent.rag;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 二次校验 Milvus 元数据，避免过期、禁用或旧版本知识进入 Prompt。 */
@Component
public class MetadataFilterModule {
    public List<RagDocument> filter(List<RagDocument> documents, String category, String tenantId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<RagDocument> valid = documents.stream()
                .filter(document -> notBlank(document.documentId()))
                .filter(document -> notBlank(document.chunkId()))
                .filter(document -> notBlank(document.source()))
                .filter(document -> notBlank(document.content()))
                .filter(document -> category == null || category.isBlank()
                        || category.equalsIgnoreCase(document.category()))
                .filter(document -> tenantId == null || tenantId.isBlank()
                        || tenantId.equals(document.tenantId()))
                .filter(document -> document.status() == null || document.status().isBlank()
                        || "ACTIVE".equalsIgnoreCase(document.status()))
                .filter(document -> isEffective(document, now))
                .toList();

        Map<String, String> latestVersion = valid.stream()
                .collect(Collectors.toMap(
                        RagDocument::documentId,
                        document -> valueOrEmpty(document.version()),
                        (left, right) -> compareVersion(left, right) >= 0 ? left : right));
        return valid.stream()
                .filter(document -> latestVersion.get(document.documentId())
                        .equals(valueOrEmpty(document.version())))
                .toList();
    }

    private boolean isEffective(RagDocument document, LocalDateTime now) {
        LocalDateTime from = parseDateTime(document.effectiveFrom());
        LocalDateTime to = parseDateTime(document.effectiveTo());
        if ((notBlank(document.effectiveFrom()) && from == null)
                || (notBlank(document.effectiveTo()) && to == null)) {
            return false;
        }
        return (from == null || !from.isAfter(now)) && (to == null || to.isAfter(now));
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignoredAgain) {
                try {
                    return LocalDate.parse(value).atStartOfDay();
                } catch (DateTimeParseException invalid) {
                    return null;
                }
            }
        }
    }

    private int compareVersion(String left, String right) {
        return compareParts(versionParts(left), versionParts(right));
    }

    private List<Integer> versionParts(String version) {
        return java.util.Arrays.stream(version.split("[^0-9]+"))
                .filter(part -> !part.isBlank())
                .map(part -> {
                    try {
                        return Integer.parseInt(part);
                    } catch (NumberFormatException exception) {
                        return 0;
                    }
                })
                .toList();
    }

    private int compareParts(List<Integer> left, List<Integer> right) {
        for (int index = 0; index < Math.max(left.size(), right.size()); index++) {
            int leftPart = index < left.size() ? left.get(index) : 0;
            int rightPart = index < right.size() ? right.get(index) : 0;
            if (leftPart != rightPart) {
                return Integer.compare(leftPart, rightPart);
            }
        }
        return 0;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
