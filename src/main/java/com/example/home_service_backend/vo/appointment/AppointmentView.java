package com.example.home_service_backend.vo.appointment;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentView(
        Long id,
        String requestNo,
        Long userId,
        Long shopId,
        Long serviceId,
        Long addressId,
        String serviceTitle,
        String requirementText,
        LocalDateTime preferredStart,
        LocalDateTime preferredEnd,
        String contactName,
        String contactPhone,
        String status,
        Long platformOperatorId,
        String platformNote,
        String cancelReason,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        String shopName,
        AppointmentServiceView service,
        AppointmentAddressView address,
        List<AppointmentStatusHistoryView> statusHistory) {
}
