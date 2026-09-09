package com.example.home_service_backend.service;

import com.example.home_service_backend.agent.rag.MilvusKnowledgeIndexClient;
import com.example.home_service_backend.agent.rag.RagTextChunker;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.common.utils.SecurityUtils;
import com.example.home_service_backend.dto.request.rag.CreateKnowledgeDocumentRequest;
import com.example.home_service_backend.entity.KnowledgeDocument;
import com.example.home_service_backend.entity.KnowledgeDocumentChunk;
import com.example.home_service_backend.entity.KnowledgeDocumentVersion;
import com.example.home_service_backend.repository.KnowledgeDocumentChunkRepository;
import com.example.home_service_backend.repository.KnowledgeDocumentRepository;
import com.example.home_service_backend.repository.KnowledgeDocumentVersionRepository;
import com.example.home_service_backend.vo.rag.KnowledgeDocumentPageView;
import com.example.home_service_backend.vo.rag.KnowledgeDocumentView;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeDocumentService {
    private static final String ACTIVE = "ACTIVE";
    private static final String RETIRED = "RETIRED";
    private static final String INDEXED = "INDEXED";
    private static final String FAILED = "FAILED";
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeDocumentVersionRepository versionRepository;
    private final KnowledgeDocumentChunkRepository chunkRepository;
    private final RagTextChunker chunker;
    private final MilvusKnowledgeIndexClient indexClient;
    private final TransactionTemplate transactionTemplate;
    public KnowledgeDocumentService(KnowledgeDocumentRepository documentRepository,
                                    KnowledgeDocumentVersionRepository versionRepository,
                                    KnowledgeDocumentChunkRepository chunkRepository,
                                    RagTextChunker chunker, MilvusKnowledgeIndexClient indexClient,
                                    PlatformTransactionManager transactionManager) {
        this.documentRepository=documentRepository; this.versionRepository=versionRepository;
        this.chunkRepository=chunkRepository; this.chunker=chunker; this.indexClient=indexClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public KnowledgeDocumentView create(CreateKnowledgeDocumentRequest request) {
        if (request.effectiveFrom()!=null && request.effectiveTo()!=null && !request.effectiveTo().isAfter(request.effectiveFrom()))
            throw new BusinessException("400", "知识有效期结束时间必须晚于开始时间");
        PersistedKnowledge persisted = transactionTemplate.execute(status -> persist(request));
        if (persisted == null) throw new BusinessException("500", "知识文档保存失败");
        try {
            // 先移除同一 document_key 的旧向量，避免新旧版本同时被召回；MySQL 版本事实已在事务中保存。
            indexClient.delete(persisted.document().getDocumentKey());
            indexClient.index(persisted.index());
            transactionTemplate.executeWithoutResult(status -> markIndexed(persisted));
        } catch (RuntimeException e) {
            transactionTemplate.executeWithoutResult(status -> markFailed(persisted, e));
        }
        return get(persisted.document().getId());
    }

    public KnowledgeDocumentPageView list(int page,int size){ var p=documentRepository.findByStatusOrderByUpdatedTimeDesc(ACTIVE, PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"updatedTime"))); return new KnowledgeDocumentPageView(p.getContent().stream().map(d -> toView(d, versionRepository.findTopByDocumentIdOrderByVersionNoDesc(d.getId()).orElse(null))).toList(), p.getTotalElements(), page,size); }
    public KnowledgeDocumentView get(Long id){ KnowledgeDocument d=documentRepository.findById(id).orElseThrow(()->new BusinessException("404","知识文档不存在")); return toView(d,versionRepository.findTopByDocumentIdOrderByVersionNoDesc(id).orElse(null)); }
    public void delete(Long id){
        String documentKey = transactionTemplate.execute(status -> markDeleted(id));
        if (documentKey == null) throw new BusinessException("404", "知识文档不存在");
        indexClient.delete(documentKey);
    }

    /** 重新索引当前文档的最新版本；适用于 Embedding/Milvus 暂时不可用后的恢复。 */
    public KnowledgeDocumentView reindex(Long id) {
        PersistedKnowledge persisted = transactionTemplate.execute(status -> loadForReindex(id));
        if (persisted == null) throw new BusinessException("404", "知识文档不存在");
        try {
            indexClient.delete(persisted.document().getDocumentKey());
            indexClient.index(persisted.index());
            transactionTemplate.executeWithoutResult(status -> markIndexed(persisted));
        } catch (RuntimeException e) {
            transactionTemplate.executeWithoutResult(status -> markFailed(persisted, e));
        }
        return get(id);
    }
    public KnowledgeDocumentView seed(){ try { String content=new String(new ClassPathResource("knowledge/home_service_faq.md").getInputStream().readAllBytes(), StandardCharsets.UTF_8); return create(new CreateKnowledgeDocumentRequest("home-service-faq", "家政服务常见问题", "classpath:knowledge/home_service_faq.md", "knowledge", null, content, null, null)); } catch(Exception e){ throw new BusinessException("500","内置知识库读取失败"); } }
    private PersistedKnowledge persist(CreateKnowledgeDocumentRequest request) {
        long userId = SecurityUtils.requireLoginUser().getId();
        KnowledgeDocument document = documentRepository.findByDocumentKey(request.documentKey().trim()).orElseGet(() -> {
            KnowledgeDocument d=new KnowledgeDocument(); d.setDocumentKey(request.documentKey().trim()); d.setCurrentVersion(0); d.setCreatedTime(now()); d.setCreatedBy(userId); return d;
        });
        document.setTitle(request.title().trim()); document.setSource(request.source().trim()); document.setCategory(request.category().trim());
        document.setTenantId(blankToNull(request.tenantId())); document.setStatus(ACTIVE); document.setUpdatedTime(now());
        document = documentRepository.saveAndFlush(document);
        int versionNo = versionRepository.findTopByDocumentIdOrderByVersionNoDesc(document.getId()).map(v -> v.getVersionNo()+1).orElse(1);
        List<String> pieces = chunker.split(request.content());
        if (pieces.isEmpty()) throw new BusinessException("400", "知识正文不能为空");
        KnowledgeDocumentVersion version=new KnowledgeDocumentVersion(); version.setDocumentId(document.getId()); version.setVersionNo(versionNo);
        version.setContent(request.content().trim()); version.setChecksum(sha256(request.content())); version.setStatus("INDEXING");
        version.setEffectiveFrom(request.effectiveFrom()); version.setEffectiveTo(request.effectiveTo()); version.setChunkCount(pieces.size()); version.setCreatedBy(userId); version.setCreatedTime(now());
        version=versionRepository.saveAndFlush(version);
        List<KnowledgeDocumentChunk> chunks=new ArrayList<>();
        for(int i=0;i<pieces.size();i++){ KnowledgeDocumentChunk c=new KnowledgeDocumentChunk(); c.setDocumentId(document.getId()); c.setVersionId(version.getId()); c.setChunkId(document.getDocumentKey()+"-v"+versionNo+"-c"+i); c.setChunkIndex(i); c.setContent(pieces.get(i)); c.setStatus("PENDING"); c.setVectorId(c.getChunkId()); c.setCreatedTime(now()); c.setUpdatedTime(now()); chunks.add(chunkRepository.save(c)); }
        document.setCurrentVersion(versionNo); documentRepository.save(document);
        return persisted(document, version, chunks);
    }

    private PersistedKnowledge loadForReindex(Long id) {
        KnowledgeDocument document = documentRepository.findById(id).orElse(null);
        if (document == null || !ACTIVE.equals(document.getStatus())) return null;
        KnowledgeDocumentVersion version = versionRepository.findByDocumentIdAndVersionNo(id, document.getCurrentVersion()).orElse(null);
        if (version == null) return null;
        return persisted(document, version, chunkRepository.findByVersionIdOrderByChunkIndexAsc(version.getId()));
    }

    private PersistedKnowledge persisted(KnowledgeDocument document, KnowledgeDocumentVersion version, List<KnowledgeDocumentChunk> chunks) {
        return new PersistedKnowledge(document, version, chunks, new MilvusKnowledgeIndexClient.KnowledgeDocumentIndex(document.getDocumentKey(), document.getTitle(), document.getSource(), document.getCategory(), document.getTenantId(), version.getVersionNo(), version.getEffectiveFrom(), version.getEffectiveTo(), chunks.stream().map(c -> new MilvusKnowledgeIndexClient.KnowledgeChunkIndex(c.getChunkId(), c.getContent())).toList()));
    }
    private void markIndexed(PersistedKnowledge p) { KnowledgeDocumentVersion v=versionRepository.findById(p.version().getId()).orElseThrow(); v.setStatus(ACTIVE); v.setIndexError(null); versionRepository.save(v); versionRepository.findByDocumentIdOrderByVersionNoDesc(p.document().getId()).forEach(other -> { if (!other.getId().equals(v.getId()) && !RETIRED.equals(other.getStatus())) { other.setStatus(RETIRED); versionRepository.save(other); } }); List<KnowledgeDocumentChunk> chunks=chunkRepository.findByVersionIdOrderByChunkIndexAsc(v.getId()); chunks.forEach(c->{c.setStatus(INDEXED);c.setUpdatedTime(now());}); chunkRepository.saveAll(chunks); }
    private void markFailed(PersistedKnowledge p, RuntimeException e) { KnowledgeDocumentVersion v=versionRepository.findById(p.version().getId()).orElseThrow(); v.setStatus(FAILED); v.setIndexError(trimError(e.getMessage())); versionRepository.save(v); List<KnowledgeDocumentChunk> chunks=chunkRepository.findByVersionIdOrderByChunkIndexAsc(v.getId()); chunks.forEach(c->{c.setStatus(FAILED);c.setUpdatedTime(now());}); chunkRepository.saveAll(chunks); }
    private String markDeleted(Long id) { KnowledgeDocument d=documentRepository.findById(id).orElse(null); if(d==null)return null; d.setStatus("DELETED");d.setUpdatedTime(now());documentRepository.save(d);versionRepository.findByDocumentIdOrderByVersionNoDesc(id).forEach(v->{if(!RETIRED.equals(v.getStatus())){v.setStatus(RETIRED);versionRepository.save(v);}});List<KnowledgeDocumentChunk> chunks=chunkRepository.findByDocumentId(id);chunks.forEach(c->{c.setStatus("DELETED");c.setUpdatedTime(now());});chunkRepository.saveAll(chunks);return d.getDocumentKey(); }
    private String trimError(String value) { if (value == null) return "向量索引失败"; return value.length() <= 1024 ? value : value.substring(0, 1024); }
    private record PersistedKnowledge(KnowledgeDocument document, KnowledgeDocumentVersion version, List<KnowledgeDocumentChunk> chunks, MilvusKnowledgeIndexClient.KnowledgeDocumentIndex index) {}
    private KnowledgeDocumentView toView(KnowledgeDocument d, KnowledgeDocumentVersion v){ return new KnowledgeDocumentView(d.getId(),d.getDocumentKey(),d.getTitle(),d.getSource(),d.getCategory(),d.getTenantId(),d.getCurrentVersion(),d.getStatus(),v==null?null:v.getStatus(),v==null?null:v.getChunkCount(),v==null?null:v.getEffectiveFrom(),v==null?null:v.getEffectiveTo(),v==null?null:v.getIndexError(),d.getUpdatedTime()); }
    private LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC);} private String blankToNull(String v){return v==null||v.isBlank()?null:v.trim();}
    private String sha256(String v){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
}
