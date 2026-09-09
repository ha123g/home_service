package com.example.home_service_backend.agent.skills.recommendation;

import com.example.home_service_backend.agent.dto.internal.AgentContext;
import com.example.home_service_backend.agent.dto.internal.AgentResult;
import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.agent.prompts.AgentPrompts;
import com.example.home_service_backend.agent.skills.AgentSkill;
import com.example.home_service_backend.agent.tools.ShopRecommendationTool;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐智能体
 */
@Component
public class RecommendationAgent implements AgentSkill {
    private static final Logger log = LoggerFactory.getLogger(RecommendationAgent.class);
    public static final String MODEL_NAME = "qwen3.8-27b";
    private static final int MAX_RESULTS = 5;

    private final ShopRecommendationTool recommendationTool;
    private final AgentModelFactory modelFactory;
    private final RecommendationCriteriaParser criteriaParser;

    public RecommendationAgent(
            ShopRecommendationTool recommendationTool,
            AgentModelFactory modelFactory,
            RecommendationCriteriaParser criteriaParser) {
        this.recommendationTool = recommendationTool;
        this.modelFactory = modelFactory;
        this.criteriaParser = criteriaParser;
    }

    @Override
    public AgentResult execute(AgentContext context) {
        RecommendationCriteria criteria = criteriaParser.parse(context.message(), context.latitude(), context.longitude());
        var shops = recommendationTool.search(criteria, 0, MAX_RESULTS);
        String answer = shops.items().isEmpty()
                ? "暂未找到符合条件的营业商家，请尝试补充城市、区域或服务类型。"
                : "为你找到 " + shops.items().size() + " 家相关商家，请从列表中查看。";
        int inputTokens = 0;
        int outputTokens = 0;
        long latencyMs = 0;
        try {
            var result = modelFactory.create(MODEL_NAME, 0.2D, 600).chat(
                    AgentPrompts.RECOMMENDATION_EXPLANATION_V1 + "\n结果："
                            + safeModelFacts(shops.items()),
                    context.message(),
                    600);
            String candidate = result.content() == null ? "" : result.content().trim();
            // 推荐卡片负责展示商家明细，回复只保留一行摘要，避免模型重复输出长列表。
            if (!candidate.isBlank() && candidate.length() <= 160
                    && !candidate.contains("SHOP_LIST") && !candidate.contains("商家推荐")) {
                answer = candidate.replaceAll("[\\r\\n]+", " ").replaceAll("^[-•*]\\s*", "").trim();
            }
            inputTokens = result.inputTokens();
            outputTokens = result.outputTokens();
            latencyMs = result.latencyMs();
        } catch (RuntimeException exception) {
            log.warn("[AI] 推荐说明模型调用失败；会话编号={}，原因={}",
                    context.sessionId(), exception.getClass().getSimpleName());
            // 结构化列表仍可用，模型不可用时返回确定性说明。
        }
        return new AgentResult(
                answer, "SHOP_LIST", "shop-list.v1", shops, null, "RECOMMENDATION",
                inputTokens, outputTokens, latencyMs);
    }

    private List<Map<String, Object>> safeModelFacts(
            List<com.example.home_service_backend.vo.merchant.ShopView> shops) {
        return shops.stream().map(shop -> {
            Map<String, Object> facts = new LinkedHashMap<>();
            facts.put("shopId", shop.id());
            facts.put("shopName", shop.shopName());
            facts.put("shopIntro", shop.shopIntro());
            facts.put("serviceArea", shop.serviceArea());
            facts.put("serviceRadiusKm", shop.serviceRadiusKm());
            facts.put("city", shop.city());
            facts.put("district", shop.district());
            facts.put("distanceKm", shop.distanceKm());
            facts.put("status", shop.status());
            facts.put("serviceNames", shop.serviceNames());
            facts.put("tags", shop.tags());
            return facts;
        }).toList();
    }
}
