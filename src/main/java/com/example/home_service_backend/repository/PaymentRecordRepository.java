package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findTopByOrderIdOrderByIdDesc(Long orderId);
    Optional<PaymentRecord> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentRecord> findByProviderPaymentId(String providerPaymentId);
}
