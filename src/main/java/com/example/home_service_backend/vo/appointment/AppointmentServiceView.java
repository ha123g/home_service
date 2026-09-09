package com.example.home_service_backend.vo.appointment;

import java.math.BigDecimal;

/** 预约时使用的商家服务详情。 */
public record AppointmentServiceView(
        Long id,
        String title,
        String summary,
        String description,
        String pricingUnit,
        BigDecimal basePrice,
        Integer durationMinutes) {
}
