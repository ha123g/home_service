package com.example.home_service_backend.repository;
import com.example.home_service_backend.entity.KnowledgeDocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface KnowledgeDocumentChunkRepository extends JpaRepository<KnowledgeDocumentChunk, Long> {
    List<KnowledgeDocumentChunk> findByVersionIdOrderByChunkIndexAsc(Long versionId);
    List<KnowledgeDocumentChunk> findByDocumentId(Long documentId);
    void deleteByVersionId(Long versionId);

    @Query("select d.documentKey as documentId, c.chunkId as chunkId, d.source as source, d.title as title, c.content as content, d.tenantId as tenantId, d.category as category, v.versionNo as version, v.status as status, v.effectiveFrom as effectiveFrom, v.effectiveTo as effectiveTo "
            + "from KnowledgeDocumentChunk c, KnowledgeDocumentVersion v, KnowledgeDocument d "
            + "where c.versionId = v.id and v.documentId = d.id and d.status = 'ACTIVE' and v.status = 'ACTIVE' "
            + "and (v.effectiveFrom is null or v.effectiveFrom <= CURRENT_TIMESTAMP) "
            + "and (v.effectiveTo is null or v.effectiveTo > CURRENT_TIMESTAMP) "
            + "and (:category is null or d.category = :category) and (:tenantId is null or d.tenantId = :tenantId) "
            + "and (c.content like concat('%', :keyword, '%') or lower(d.title) like lower(concat('%', :keyword, '%'))) "
            + "order by v.versionNo desc, c.chunkIndex asc")
    List<KnowledgeChunkSearchProjection> searchActiveByKeyword(@Param("keyword") String keyword,
                                                                @Param("category") String category,
                                                                @Param("tenantId") String tenantId);

    /** 向量索引失败时，只回退到当前文档版本的 MySQL 正文，不回退到已退休版本。 */
    @Query("select d.documentKey as documentId, c.chunkId as chunkId, d.source as source, d.title as title, c.content as content, d.tenantId as tenantId, d.category as category, v.versionNo as version, v.status as status, v.effectiveFrom as effectiveFrom, v.effectiveTo as effectiveTo "
            + "from KnowledgeDocumentChunk c, KnowledgeDocumentVersion v, KnowledgeDocument d "
            + "where c.versionId = v.id and v.documentId = d.id and d.status = 'ACTIVE' "
            + "and v.versionNo = d.currentVersion and v.status in ('ACTIVE', 'FAILED', 'INDEXING') "
            + "and (v.effectiveFrom is null or v.effectiveFrom <= CURRENT_TIMESTAMP) "
            + "and (v.effectiveTo is null or v.effectiveTo > CURRENT_TIMESTAMP) "
            + "and (:category is null or d.category = :category) and (:tenantId is null or d.tenantId = :tenantId) "
            + "and (c.content like concat('%', :keyword, '%') or lower(d.title) like lower(concat('%', :keyword, '%'))) "
            + "order by c.chunkIndex asc")
    List<KnowledgeChunkSearchProjection> searchCurrentVersionByKeyword(@Param("keyword") String keyword,
                                                                        @Param("category") String category,
                                                                        @Param("tenantId") String tenantId);
}
