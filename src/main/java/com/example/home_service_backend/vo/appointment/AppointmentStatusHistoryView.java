package com.example.home_service_backend.vo.appointment;

import java.time.LocalDateTime;

/** 预约状态变更轨迹。 */
public record AppointmentStatusHistoryView(
        Long id,
        String fromStatus,
        String toStatus,
        Long operatorUserId,
        String reason,
        LocalDateTime createdTime) {
}
