package com.example.home_service_backend.dto.request.merchant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** 商家在平台标准分类下填写的自定义服务明细。basePrice 仅表示起步/参考价。 */
public record MerchantServiceItemRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 128) String title,
        @Size(max = 512) String summary,
        @Size(max = 2048) String description,
        @NotBlank @Size(max = 16) String pricingUnit,
        @NotNull @DecimalMin("0.00") BigDecimal basePrice,
        @Positive Integer durationMinutes,
        @Size(max = 12) List<@NotBlank @Size(max = 32) String> tags) {
}
