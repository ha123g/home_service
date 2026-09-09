package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    Optional<ServiceOrder> findByOrderNo(String orderNo);
    Optional<ServiceOrder> findBySourceRequestId(Long sourceRequestId);
    Page<ServiceOrder> findByUserIdOrderByCreatedTimeDesc(Long userId, Pageable pageable);
    Page<ServiceOrder> findByShopIdOrderByCreatedTimeDesc(Long shopId, Pageable pageable);
    Page<ServiceOrder> findByUserIdOrShopIdOrderByCreatedTimeDesc(Long userId, Long shopId, Pageable pageable);
    Page<ServiceOrder> findAllByOrderByCreatedTimeDesc(Pageable pageable);
}
