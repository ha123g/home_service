package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.AppointmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, Long> {
    Optional<AppointmentRequest> findByRequestNo(String requestNo);
    Optional<AppointmentRequest> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
    Page<AppointmentRequest> findByUserIdOrderByCreatedTimeDesc(Long userId, Pageable pageable);
    Page<AppointmentRequest> findByShopIdOrderByCreatedTimeDesc(Long shopId, Pageable pageable);
    Page<AppointmentRequest> findByUserIdOrShopIdOrderByCreatedTimeDesc(Long userId, Long shopId, Pageable pageable);
    Page<AppointmentRequest> findAllByOrderByCreatedTimeDesc(Pageable pageable);
}
