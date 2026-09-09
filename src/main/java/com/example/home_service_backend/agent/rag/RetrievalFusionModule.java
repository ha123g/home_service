package com.example.home_service_backend.agent.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 使用倒数排名融合原查询和改写查询，并以轻量关键词命中作为排序补充。 */
@Component
public class RetrievalFusionModule {
    private static final double RRF_OFFSET = 60D;

    public List<RagDocument> fuse(String query, List<List<RagDocument>> rankedLists, int limit) {
        Map<String, RagDocument> documents = new LinkedHashMap<>();
        Map<String, Double> scores = new HashMap<>();
        for (List<RagDocument> rankedList : rankedLists) {
            for (int index = 0; index < rankedList.size(); index++) {
                RagDocument document = rankedList.get(index);
                String key = document.documentId() + ":" + document.chunkId();
                documents.putIfAbsent(key, document);
                scores.merge(key, 1D / (RRF_OFFSET + index + 1), Double::sum);
            }
        }
        documents.forEach((key, document) ->
                scores.merge(key, lexicalScore(query, document), Double::sum));
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(Math.max(1, limit))
                .map(entry -> documents.get(entry.getKey()))
                .toList();
    }

    private double lexicalScore(String query, RagDocument document) {
        String searchable = ((document.title() == null ? "" : document.title()) + " "
                + document.content()).toLowerCase(Locale.ROOT);
        List<String> terms = terms(query);
        if (terms.isEmpty()) {
            return 0D;
        }
        long matches = terms.stream().filter(searchable::contains).count();
        return (matches / (double) terms.size()) * 0.01D;
    }

    private List<String> terms(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String normalized = query.toLowerCase(Locale.ROOT).replaceAll("[\\p{Punct}\\s]+", "");
        List<String> terms = new ArrayList<>();
        for (int index = 0; index < normalized.length(); index += 2) {
            terms.add(normalized.substring(index, Math.min(index + 2, normalized.length())));
        }
        return terms;
    }
}
