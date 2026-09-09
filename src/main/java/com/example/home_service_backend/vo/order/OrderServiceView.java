package com.example.home_service_backend.vo.order;

import java.math.BigDecimal;

/** 订单服务详情；订单金额以订单自身金额字段为准。 */
public record OrderServiceView(
        Long id,
        String title,
        String summary,
        String description,
        String pricingUnit,
        BigDecimal referencePrice,
        Integer durationMinutes) {
}
