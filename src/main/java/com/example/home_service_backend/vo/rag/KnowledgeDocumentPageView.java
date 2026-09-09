package com.example.home_service_backend.vo.rag;
import java.util.List;
public record KnowledgeDocumentPageView(List<KnowledgeDocumentView> items, long total, int page, int size) {}
