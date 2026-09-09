package com.example.home_service_backend.agent.tools;

import com.example.home_service_backend.agent.skills.recommendation.RecommendationCriteria;
import com.example.home_service_backend.service.ShopService;
import com.example.home_service_backend.vo.merchant.ShopPageView;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/** 只读商家推荐 Tool；所有条件由后端 Service 再次校验。 */
@Component
public class ShopRecommendationTool {
    private final ShopService shopService;

    public ShopRecommendationTool(ShopService shopService) {
        this.shopService = shopService;
    }

    @Tool(name = "shop_recommendation_search", description = "按结构化条件检索公开营业商家，最多返回五条，只读")
    public ShopPageView search(
            @ToolParam(description = "经过校验的商家检索条件") RecommendationCriteria criteria,
            @ToolParam(description = "从零开始的分页页码") int page,
            @ToolParam(description = "每页数量，服务端会限制上限") int size) {
        return shopService.search(
                criteria.keyword(), criteria.province(), criteria.city(), criteria.district(), null,
                criteria.categoryId(), criteria.latitude(), criteria.longitude(), criteria.radiusKm(),
                page, Math.min(size, 5));
    }
}
