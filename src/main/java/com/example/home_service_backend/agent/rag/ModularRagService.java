package com.example.home_service_backend.agent.rag;

import com.example.home_service_backend.agent.config.AgentRagProperties;
import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.agent.model.EmbeddingClient;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.repository.KnowledgeChunkSearchProjection;
import com.example.home_service_backend.repository.KnowledgeDocumentChunkRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Modular RAG 查询编排；向量库只承载知识索引，业务事实仍来自 MySQL。 */
@Service
public class ModularRagService {
    private static final Logger log = LoggerFactory.getLogger(ModularRagService.class);
    private final AgentRagProperties properties;
    private final AgentModelFactory modelFactory;
    private final QueryRewriteModule queryRewrite;
    private final MilvusVectorSearchClient vectorSearch;
    private final MetadataFilterModule metadataFilter;
    private final RetrievalFusionModule fusion;
    private final KnowledgeDocumentChunkRepository chunkRepository;

    public ModularRagService(
            AgentRagProperties properties,
            AgentModelFactory modelFactory,
            QueryRewriteModule queryRewrite,
            MilvusVectorSearchClient vectorSearch,
            MetadataFilterModule metadataFilter,
            RetrievalFusionModule fusion,
            KnowledgeDocumentChunkRepository chunkRepository) {
        this.properties = properties;
        this.modelFactory = modelFactory;
        this.queryRewrite = queryRewrite;
        this.vectorSearch = vectorSearch;
        this.metadataFilter = metadataFilter;
        this.fusion = fusion;
        this.chunkRepository = chunkRepository;
    }

    public List<RagDocument> search(String query, String category) {
        return search(query, category, null);
    }

    public List<RagDocument> search(String query, String category, String tenantId) {
        List<RagDocument> databaseResults = keywordFallback(query, category, tenantId);
        if (!properties.enabled() || properties.milvusBaseUrl() == null || properties.milvusBaseUrl().isBlank()) return databaseResults;
        String rewritten = queryRewrite.rewrite(query);
        List<String> queries = rewritten.equals(query) ? List.of(query) : List.of(query, rewritten);
        EmbeddingClient embedding;
        try {
            embedding = modelFactory.createEmbedding();
        } catch (RuntimeException ex) {
            // Embedding 是知识问答的增强能力，不应阻断业务/账户类对话。
            log.warn("[AI] RAG Embedding 客户端不可用，使用 MySQL 关键词降级；原因={}", ex.getClass().getSimpleName());
            return databaseResults;
        }
        List<List<RagDocument>> resultSets = new ArrayList<>();
        for (String candidate : queries) {
            List<Double> vector;
            try { vector = embedding.embed(candidate); }
            catch (RuntimeException ex) {
                log.warn("[AI] RAG Embedding 调用失败，使用 MySQL 关键词降级；原因={}", ex.getClass().getSimpleName());
                return databaseResults;
            }
            try {
                List<RagDocument> raw = vectorSearch.search(vector, category, tenantId);
                resultSets.add(metadataFilter.filter(raw, category, tenantId).stream()
                        .filter(document -> isLexicallyRelated(query, document)).toList());
            } catch (RuntimeException ex) {
                log.warn("[AI] Milvus 检索失败，使用 MySQL 关键词降级；原因={}", ex.getClass().getSimpleName());
                return databaseResults;
            }
        }
        List<RagDocument> vectorResults = fusion.fuse(query, resultSets, properties.maxContextChunks());
        if (vectorResults.isEmpty()) return databaseResults;
        return fusion.fuse(query, List.of(vectorResults, databaseResults), properties.maxContextChunks());
    }

    private List<RagDocument> keywordFallback(String query, String category, String tenantId) {
        List<String> terms = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            String normalized = query.replaceAll("[^\\p{L}\\p{N}]", " ").trim();
            for (String part : normalized.split("\\s+")) {
                if (part.length() >= 2 && !isStopTerm(part)) terms.add(part);
                // 中文没有空格分词，使用重叠二元组覆盖“保洁”“家电”等关键词。
                for (int i = 0; i + 1 < part.length() && terms.size() < 16; i++) {
                    String pair = part.substring(i, i + 2);
                    if (!isStopTerm(pair)) terms.add(pair);
                }
            }
        }
        if (terms.isEmpty()) terms = List.of(query == null ? "" : query);
        List<RagDocument> documents = new ArrayList<>();
        for (String term : terms.stream().distinct().limit(16).toList()) {
            if (term.isBlank()) continue;
            documents.addAll(chunkRepository.searchCurrentVersionByKeyword(term, blankToNull(category), blankToNull(tenantId)).stream().map(this::toDocument).toList());
        }
        return metadataFilter.filter(documents, category, tenantId).stream().distinct().limit(properties.maxContextChunks()).toList();
    }

    private RagDocument toDocument(KnowledgeChunkSearchProjection item) {
        // MySQL 文档目录是业务事实；即使 Embedding/Milvus 索引失败，仍允许当前有效版本参与关键词降级检索。
        // 这是当前有效文档的 MySQL 关键词降级结果；RagDocument.status 表示文档可见性，
        // 不把向量索引失败状态伪装成 Milvus 已完成索引。
        return new RagDocument(item.getDocumentId(), item.getChunkId(), item.getSource(), item.getTitle(), item.getContent(), item.getTenantId(), item.getCategory(), item.getVersion() == null ? null : String.valueOf(item.getVersion()), "ACTIVE", item.getEffectiveFrom() == null ? null : item.getEffectiveFrom().toString(), item.getEffectiveTo() == null ? null : item.getEffectiveTo().toString());
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private boolean isLexicallyRelated(String query, RagDocument document) {
        String searchable = ((document.title() == null ? "" : document.title()) + " "
                + (document.content() == null ? "" : document.content())).toLowerCase();
        return meaningfulTerms(query).stream().anyMatch(searchable::contains);
    }

    private List<String> meaningfulTerms(String query) {
        if (query == null || query.isBlank()) return List.of();
        String normalized = query.toLowerCase().replaceAll("[^\\p{L}\\p{N}]", "");
        List<String> terms = new ArrayList<>();
        for (int i = 0; i + 1 < normalized.length(); i++) {
            String pair = normalized.substring(i, i + 2);
            if (!isStopTerm(pair)) terms.add(pair);
        }
        return terms;
    }

    private boolean isStopTerm(String term) {
        return Set.of("请问", "帮我", "告诉", "如何", "怎么", "什么", "哪些", "需要", "提供", "一下", "有关", "关于", "可以", "是否", "用户", "服务").contains(term);
    }
}
