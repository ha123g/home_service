package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.AiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {
    long countByStatus(String status);
    Page<AiRequestLog> findAllByOrderByCreatedTimeDesc(Pageable pageable);
}
