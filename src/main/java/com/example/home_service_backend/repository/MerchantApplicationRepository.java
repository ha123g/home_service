package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.MerchantApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MerchantApplicationRepository extends JpaRepository<MerchantApplication, Long> {
    long countByStatus(String status);
    Optional<MerchantApplication> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    Page<MerchantApplication> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<MerchantApplication> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
