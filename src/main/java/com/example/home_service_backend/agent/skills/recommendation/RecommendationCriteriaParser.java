package com.example.home_service_backend.agent.skills.recommendation;

import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.agent.model.ChatModelClient;
import com.example.home_service_backend.agent.prompts.AgentPrompts;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/** 使用受限 JSON 从自然语言提取商家检索条件；任何异常都降级为关键词检索。 */
@Component
public class RecommendationCriteriaParser {
    private static final Logger log = LoggerFactory.getLogger(RecommendationCriteriaParser.class);
    private static final ChatModelClient.ToolDefinition SEARCH_TOOL = new ChatModelClient.ToolDefinition(
            "shop_recommendation_search",
            "从用户自然语言中提取结构化条件并检索真实营业商家；未知条件必须省略",
            Map.of(
                    "type", "object",
                    "additionalProperties", false,
                    "properties", Map.of(
                            "criteria", Map.of(
                                    "type", "object",
                                    "additionalProperties", false,
                                    "properties", Map.of(
                                            "keyword", Map.of("type", "string", "description", "服务名称或关键词；未知时留空"),
                                            "province", Map.of("type", "string", "description", "省；未知时留空"),
                                            "city", Map.of("type", "string", "description", "市；未知时留空"),
                                            "district", Map.of("type", "string", "description", "区县；未知时留空"),
                                            "categoryId", Map.of("type", "integer", "description", "平台分类编号；未知时省略"),
                                            "latitude", Map.of("type", "number", "description", "用户明确提供的纬度；未知时省略"),
                                            "longitude", Map.of("type", "number", "description", "用户明确提供的经度；未知时省略"),
                                            "radiusKm", Map.of("type", "number", "description", "用户明确提供的半径公里数；未知时省略"))),
                            "page", Map.of("type", "integer", "description", "固定为 0"),
                            "size", Map.of("type", "integer", "description", "固定为 5")),
                    "required", List.of("criteria")));
    private final AgentModelFactory modelFactory;
    private final ObjectMapper objectMapper;

    public RecommendationCriteriaParser(AgentModelFactory modelFactory, ObjectMapper objectMapper) {
        this.modelFactory = modelFactory;
        this.objectMapper = objectMapper;
    }

    public RecommendationCriteria parse(String message) {
        return parse(message, null, null);
    }

    public RecommendationCriteria parse(String message, java.math.BigDecimal userLatitude,
                                        java.math.BigDecimal userLongitude) {
        RecommendationCriteria parsed;
        try {
            var result = modelFactory.create(RecommendationAgent.MODEL_NAME, 0.0D, 300)
                    .callTools(AgentPrompts.RECOMMENDATION_FUNCTION_CALLING_V2,
                            message, List.of(SEARCH_TOOL), 300);
            var call = result.toolCalls().stream()
                    .filter(item -> "shop_recommendation_search".equals(item.name()))
                    .findFirst().orElseThrow();
            JsonNode arguments = objectMapper.readTree(call.argumentsJson());
            JsonNode criteriaNode = arguments.has("criteria") ? arguments.path("criteria") : arguments;
            parsed = validate(objectMapper.treeToValue(criteriaNode, RecommendationCriteria.class));
        } catch (RuntimeException | com.fasterxml.jackson.core.JsonProcessingException exception) {
            log.warn("[AI] 推荐条件模型调用失败，使用关键词检索；原因={}", exception.getClass().getSimpleName());
            parsed = new RecommendationCriteria(trimToLength(extractKeyword(message), 128), null, null, null,
                    null, null, null, null);
        }
        if (parsed.latitude() == null && parsed.longitude() == null
                && userLatitude != null && userLongitude != null
                && containsAny(message, "附近", "周边", "本地", "离我近")) {
            return new RecommendationCriteria(parsed.keyword(), parsed.province(), parsed.city(), parsed.district(),
                    parsed.categoryId(), userLatitude, userLongitude, 50D);
        }
        return parsed;
    }

    private RecommendationCriteria validate(RecommendationCriteria criteria) {
        boolean completeLocation = criteria.latitude() != null
                && criteria.longitude() != null
                && criteria.radiusKm() != null;
        boolean validLocation = completeLocation
                && criteria.latitude().doubleValue() >= -90D
                && criteria.latitude().doubleValue() <= 90D
                && criteria.longitude().doubleValue() >= -180D
                && criteria.longitude().doubleValue() <= 180D
                && criteria.radiusKm() > 0D
                && criteria.radiusKm() <= 500D;
        return new RecommendationCriteria(
                trimToLength(normalizeKeyword(criteria.keyword()), 128),
                trimToLength(criteria.province(), 64),
                trimToLength(criteria.city(), 64),
                trimToLength(criteria.district(), 64),
                criteria.categoryId() != null && criteria.categoryId() > 0 ? criteria.categoryId() : null,
                validLocation ? criteria.latitude() : null,
                validLocation ? criteria.longitude() : null,
                validLocation ? criteria.radiusKm() : null);
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private String extractKeyword(String message) {
        if (message == null) return null;
        String value = message.replaceAll("[，。！？!?、]", " ")
                .replaceAll("(有没有|是否有|请问|帮我找|帮我推荐|附近的|附近|推荐|商家|服务|吗|呢)", " ")
                .replaceAll("\\s+", " ").trim();
        return value.isBlank() ? message : value;
    }

    private String normalizeKeyword(String value) {
        if (value == null) return null;
        String normalized = value.replaceAll("(有没有|是否有|请问|帮我找|帮我推荐|附近的|附近|推荐|商家|服务|吗|呢)", " ")
                .replaceAll("\\s+", " ").trim();
        return normalized.isBlank() ? value.trim() : normalized;
    }

    private boolean containsAny(String value, String... candidates) {
        if (value == null) return false;
        for (String candidate : candidates) if (value.contains(candidate)) return true;
        return false;
    }
}
