package com.example.home_service_backend.vo.merchant;

import java.math.BigDecimal;
import java.util.List;

/** 入驻申请中商家填写的服务明细；起步价不是订单最终价格。 */
public record MerchantServiceItemView(
        Long categoryId,
        String categoryName,
        String title,
        String summary,
        String description,
        String pricingUnit,
        BigDecimal basePrice,
        Integer durationMinutes,
        List<String> tags) {
}
