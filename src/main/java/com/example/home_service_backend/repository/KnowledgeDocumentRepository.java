package com.example.home_service_backend.repository;
import com.example.home_service_backend.entity.KnowledgeDocument;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<KnowledgeDocument> findByDocumentKey(String documentKey);
    Page<KnowledgeDocument> findByStatusOrderByUpdatedTimeDesc(String status, Pageable pageable);
}
