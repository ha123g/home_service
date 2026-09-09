package com.example.home_service_backend.repository;
import com.example.home_service_backend.entity.KnowledgeDocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface KnowledgeDocumentVersionRepository extends JpaRepository<KnowledgeDocumentVersion, Long> {
    Optional<KnowledgeDocumentVersion> findTopByDocumentIdOrderByVersionNoDesc(Long documentId);
    Optional<KnowledgeDocumentVersion> findByDocumentIdAndVersionNo(Long documentId, Integer versionNo);
    List<KnowledgeDocumentVersion> findByDocumentIdOrderByVersionNoDesc(Long documentId);
}
