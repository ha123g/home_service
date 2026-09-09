package com.example.home_service_backend.agent.skills.recommendation;

import java.math.BigDecimal;

/** 模型提取后的非可信检索条件，使用前必须由解析器完成白名单和范围校验。 */
public record RecommendationCriteria(
        String keyword,
        String province,
        String city,
        String district,
        Long categoryId,
        BigDecimal latitude,
        BigDecimal longitude,
        Double radiusKm) {
}
