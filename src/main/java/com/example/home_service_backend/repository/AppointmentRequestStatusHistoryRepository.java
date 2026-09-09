package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.AppointmentRequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRequestStatusHistoryRepository extends JpaRepository<AppointmentRequestStatusHistory, Long> {
    List<AppointmentRequestStatusHistory> findByRequestIdOrderByCreatedTimeAsc(Long requestId);
}
