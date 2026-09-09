package com.example.home_service_backend.vo.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderView(
        Long id,
        String orderNo,
        Long userId,
        Long shopId,
        Long serviceId,
        Long workerId,
        Long addressId,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        String serviceAddressSnapshot,
        String serviceTitleSnapshot,
        String workerNameSnapshot,
        BigDecimal originAmount,
        BigDecimal payableAmount,
        Long sourceRequestId,
        LocalDateTime publishedTime,
        LocalDateTime confirmedTime,
        String status,
        String remark,
        LocalDateTime expireTime,
        LocalDateTime paidTime,
        LocalDateTime cancelledTime,
        String paymentProviderId,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        String shopName,
        OrderServiceView service,
        OrderAddressView address,
        List<OrderStatusHistoryView> statusHistory) {
}
