package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.ServiceOrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceOrderStatusHistoryRepository extends JpaRepository<ServiceOrderStatusHistory, Long> {
    List<ServiceOrderStatusHistory> findByOrderIdOrderByCreatedTimeAsc(Long orderId);
}
