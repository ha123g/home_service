package com.example.home_service_backend.agent.service;

import com.example.home_service_backend.agent.dto.internal.AgentContext;
import com.example.home_service_backend.agent.dto.internal.AgentResult;
import com.example.home_service_backend.agent.dto.request.AgentChatRequest;
import com.example.home_service_backend.agent.memory.AgentMemoryService;
import com.example.home_service_backend.agent.common.utils.AgentInputSanitizerUtil;
import com.example.home_service_backend.agent.skills.business.BusinessAgent;
import com.example.home_service_backend.agent.skills.knowledge.KnowledgeAgent;
import com.example.home_service_backend.agent.skills.recommendation.RecommendationAgent;
import com.example.home_service_backend.agent.skills.router.RouterAgent;
import com.example.home_service_backend.agent.skills.summary.SummaryAgent;
import com.example.home_service_backend.agent.vo.AgentCapabilitiesView;
import com.example.home_service_backend.agent.vo.AgentResponse;
import com.example.home_service_backend.common.utils.SecurityUtils;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.security.LoginUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** 总控 Agent：一次受限路由，顺序调用最多三个专用 Agent，最后由总结 Agent 汇总。 */
@Service
public class AgentApplicationService {
    private final RouterAgent routerAgent;
    private final KnowledgeAgent knowledgeAgent;
    private final RecommendationAgent recommendationAgent;
    private final BusinessAgent businessAgent;
    private final SummaryAgent summaryAgent;
    private final AgentMemoryService memoryService;
    private final AgentInputSanitizerUtil inputSanitizer;
    private final AgentAuditService auditService;

    public AgentApplicationService(
            RouterAgent routerAgent,
            KnowledgeAgent knowledgeAgent,
            RecommendationAgent recommendationAgent,
            BusinessAgent businessAgent,
            SummaryAgent summaryAgent,
            AgentMemoryService memoryService,
            AgentInputSanitizerUtil inputSanitizer,
            AgentAuditService auditService) {
        this.routerAgent = routerAgent;
        this.knowledgeAgent = knowledgeAgent;
        this.recommendationAgent = recommendationAgent;
        this.businessAgent = businessAgent;
        this.summaryAgent = summaryAgent;
        this.memoryService = memoryService;
        this.inputSanitizer = inputSanitizer;
        this.auditService = auditService;
    }

    public AgentResponse chat(AgentChatRequest request) {
        LoginUser user = SecurityUtils.requireLoginUser();
        String requestId = UUID.randomUUID().toString();
        long start = System.nanoTime();
        String userId = String.valueOf(user.getId());
        String route = "UNKNOWN";
        try {
            String oldSummary = memoryService.get(userId, request.sessionId());
            String safeMessage = inputSanitizer.sanitizeForModel(request.message());
            AgentContext context = new AgentContext(
                    user.getId(), request.sessionId(), safeMessage, oldSummary,
                    request.latitude(), request.longitude());
            List<String> routes = routerAgent.routes(safeMessage);
            if (routes == null || routes.isEmpty()) routes = List.of("KNOWLEDGE");
            routes = routes.stream().filter(Set.of("KNOWLEDGE", "RECOMMENDATION", "BUSINESS")::contains).distinct().limit(3).toList();
            if (routes.isEmpty()) routes = List.of("KNOWLEDGE");
            route = String.join(",", routes);
            List<AgentResult> results = new ArrayList<>();
            for (String selectedRoute : routes) {
                results.add(switch (selectedRoute) {
                    case "RECOMMENDATION" -> recommendationAgent.execute(context);
                    case "BUSINESS" -> businessAgent.execute(context);
                    default -> knowledgeAgent.execute(context);
                });
            }
            AgentResult result = aggregate(results);
            // 单路由业务办理/商家推荐直接使用专用 Agent 的结果；知识问答需要
            // 结合检索上下文整理，多路由结果也需要统一合并。避免所有回答都被
            // 同一套总结话术重写。
            String finalAnswer = requiresFinalSynthesis(routes, result)
                    ? sanitizeAnswer(summaryAgent.finalizeResponse(context, result))
                    : sanitizeAnswer(result.answer());
            String newSummary = summaryAgent.summarize(context, finalAnswer);
            memoryService.putSummary(userId, request.sessionId(), newSummary);
            List<com.example.home_service_backend.agent.rag.RagDocument> citations =
                    result.citations() == null ? List.of() : result.citations();
            // 记录成功
            auditService.recordSuccess(
                    requestId, user.getId(), routes.getFirst(), result.componentType(), modelName(routes.getFirst()),
                    result.inputTokens(), result.outputTokens(), citations.size(), elapsedMillis(start));
            return new AgentResponse(
                    route,
                    finalAnswer,
                    result.componentType(),
                    result.schemaVersion(),
                    result.data(),
                    citations,
                    requestId);
        } catch (RuntimeException exception) {
            String errorCode = exception instanceof BusinessException businessException
                    ? businessException.getCode()
                    : "AI_SYSTEM_ERROR";
            auditService.recordFailure(
                    requestId, user.getId(), route, modelName(route), elapsedMillis(start), errorCode);
            throw exception;
        }
    }

    public AgentCapabilitiesView capabilities() {
        return new AgentCapabilitiesView(
                "MULTI_ROUTE_MAX_3",
                List.of("ROUTER", "KNOWLEDGE", "RECOMMENDATION", "BUSINESS", "SUMMARY"),
                List.of("TEXT", "SHOP_LIST", "APPOINTMENT_FORM", "PAYMENT_CONFIRM",
                        "MERCHANT_APPLICATION_FORM", "PROFILE_FORM", "PASSWORD_FORM",
                        "BUSINESS_ENTRY", "BUSINESS_HELP"),
                List.of("WORKER_MANAGEMENT", "AUTO_DISPATCH", "COUPON", "COMPLAINT",
                        "REFUND_AUTOMATION"));
    }

    private String modelName(String route) {
        return switch (route) {
            case "BUSINESS" -> BusinessAgent.MODEL_NAME;
            case "RECOMMENDATION" -> RecommendationAgent.MODEL_NAME;
            case "KNOWLEDGE" -> KnowledgeAgent.MODEL_NAME;
            default -> RouterAgent.MODEL_NAME;
        };
    }

    private AgentResult aggregate(List<AgentResult> results) {
        AgentResult primary = results.stream()
                .filter(item -> item.componentType() != null && (item.componentType().endsWith("_FORM") || "PAYMENT_CONFIRM".equals(item.componentType())))
                .findFirst().orElseGet(() -> results.stream()
                // 业务查询结果（订单/预约/入驻状态）携带结构化 data，优先于知识 Agent
                // 的说明，避免多路由时把查询意图误合成为预约表单。
                .filter(item -> item.data() != null && "TEXT".equals(item.componentType()))
                .findFirst().orElseGet(() -> results.stream()
                .filter(item -> item.componentType() != null && !"TEXT".equals(item.componentType()))
                .findFirst().orElse(results.getFirst())));
        boolean authoritativeBusinessResult = "BUSINESS".equals(primary.route())
                && (primary.data() != null || (primary.componentType() != null
                && (primary.componentType().endsWith("_FORM") || "PAYMENT_CONFIRM".equals(primary.componentType()))));
        String answer = authoritativeBusinessResult
                ? primary.answer()
                : results.stream().map(AgentResult::answer)
                    .filter(item -> item != null && !item.isBlank()).distinct()
                    .reduce((a, b) -> a + "\n\n" + b).orElse("我已准备好继续协助你。");
        java.util.LinkedHashMap<String, com.example.home_service_backend.agent.rag.RagDocument> uniqueCitations = new java.util.LinkedHashMap<>();
        (authoritativeBusinessResult ? java.util.stream.Stream.of(primary) : results.stream())
                .flatMap(item -> item.citations() == null ? java.util.stream.Stream.empty() : item.citations().stream())
                .forEach(citation -> uniqueCitations.putIfAbsent(
                        String.valueOf(citation.documentId()) + ":" + citation.title() + ":" + citation.source(), citation));
        List<com.example.home_service_backend.agent.rag.RagDocument> citations = List.copyOf(uniqueCitations.values());
        int input = results.stream().mapToInt(AgentResult::inputTokens).sum();
        int output = results.stream().mapToInt(AgentResult::outputTokens).sum();
        long latency = results.stream().mapToLong(AgentResult::latencyMs).sum();
        String routes = results.stream().map(AgentResult::route).distinct().reduce((a, b) -> a + "," + b).orElse(primary.route());
        return new AgentResult(answer, primary.componentType(), primary.schemaVersion(), primary.data(), citations, routes, input, output, latency);
    }

    private long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    private boolean requiresFinalSynthesis(List<String> routes, AgentResult result) {
        return routes.size() > 1 || "KNOWLEDGE".equals(result.route());
    }

    /** 防止模型把内部路由/组件标识泄露到用户消息中。 */
    private String sanitizeAnswer(String answer) {
        if (answer == null || answer.isBlank()) return "我已准备好继续协助你。";
        String cleaned = answer.replaceAll("(?im)^\\s*(知识问答|业务办理|商家推荐)?\\s*(TEXT|SHOP_LIST|APPOINTMENT_FORM|PROFILE_FORM|PASSWORD_FORM|MERCHANT_APPLICATION_FORM|BUSINESS_HELP)\\s*$", "")
                .replaceAll("(?i)(知识问答|业务办理|商家推荐)\\s*(TEXT|SHOP_LIST|APPOINTMENT_FORM|PROFILE_FORM|PASSWORD_FORM|MERCHANT_APPLICATION_FORM|BUSINESS_HELP)", "")
                // 模型偶尔会把组件名包在列表符号、反引号或标点中；内部标识一律不对用户展示。
                .replaceAll("(?i)(?<![A-Za-z0-9_])(TEXT|SHOP_LIST|APPOINTMENT_FORM|PROFILE_FORM|PASSWORD_FORM|MERCHANT_APPLICATION_FORM|BUSINESS_HELP|BUSINESS_ENTRY)(?![A-Za-z0-9_])", "")
                .replaceAll("(?im)^\\s*(路由|组件|componentType)\\s*[:：].*$", "")
                .replaceAll("(?im)^\\s*[-*•]\\s*$", "")
                .trim();
        return cleaned.isBlank() ? "我已准备好继续协助你。" : cleaned;
    }
}
