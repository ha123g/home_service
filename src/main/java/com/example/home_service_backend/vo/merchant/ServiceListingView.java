package com.example.home_service_backend.vo.merchant;

import java.math.BigDecimal;

public record ServiceListingView(Long id, Long shopId, String title, String summary,
                                 String description, String pricingUnit, BigDecimal basePrice,
                                 Integer durationMinutes, String status) {
}
