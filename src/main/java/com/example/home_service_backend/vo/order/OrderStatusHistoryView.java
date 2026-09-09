package com.example.home_service_backend.vo.order;

import java.time.LocalDateTime;

/** 订单状态变更轨迹。 */
public record OrderStatusHistoryView(
        Long id,
        String fromStatus,
        String toStatus,
        Long operatorUserId,
        String reason,
        LocalDateTime createdTime) {
}
