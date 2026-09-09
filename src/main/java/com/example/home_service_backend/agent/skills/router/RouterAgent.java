package com.example.home_service_backend.agent.skills.router;

import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.agent.model.ChatModelClient;
import com.example.home_service_backend.agent.prompts.AgentPrompts;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 总控路由 Agent：只返回最多三个固定路由，不执行循环分发。 */
@Component
public class RouterAgent {
    private static final Logger log = LoggerFactory.getLogger(RouterAgent.class);
    public static final String MODEL_NAME = "qwen3.8-27b";
    private static final Map<String, String> ROUTE_TO_AGENT = Map.of(
            "route_knowledge", "KNOWLEDGE",
            "route_recommendation", "RECOMMENDATION",
            "route_business", "BUSINESS");
    private static final List<ChatModelClient.ToolDefinition> ROUTE_TOOLS = List.of(
            ChatModelClient.ToolDefinition.withoutArguments("route_knowledge",
                    "调用知识问答智能体：解释平台规则、流程、服务常识和注意事项，不查询用户私有业务数据"),
            ChatModelClient.ToolDefinition.withoutArguments("route_recommendation",
                    "调用检索推荐智能体：根据服务类型、地区、距离检索真实营业商家和服务"),
            ChatModelClient.ToolDefinition.withoutArguments("route_business",
                    "调用业务办理智能体：查询或办理当前用户的预约、订单、支付、个人资料、密码和商家入驻"));
    private final AgentModelFactory modelFactory;

    public RouterAgent(AgentModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    public List<String> routes(String message) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (modelFactory != null) {
            try {
                var result = modelFactory.create(MODEL_NAME, 0.0D, 160)
                        .callTools(AgentPrompts.ROUTER_FUNCTION_CALLING_V3, message, ROUTE_TOOLS, 160);
                LinkedHashSet<String> selected = new LinkedHashSet<>();
                result.toolCalls().stream()
                        .map(call -> ROUTE_TO_AGENT.get(call.name()))
                        .filter(java.util.Objects::nonNull)
                        .limit(3)
                        .forEach(selected::add);
                if (!selected.isEmpty()) return List.copyOf(selected);
            } catch (RuntimeException exception) {
                log.warn("[AI] 路由智能体调用工具失败，使用规则兜底；原因={}",
                        exception.getClass().getSimpleName());
            }
        }
        // 仅在模型不可用、协议响应无效或单元测试未配置模型时启用确定性兜底。
        return fallbackRoutes(normalized);
    }

    private List<String> fallbackRoutes(String normalized) {
        Set<String> fallback = new LinkedHashSet<>();
        if (containsAny(normalized, "预约", "订单", "支付", "取消", "地址", "填写", "入驻", "资料", "个人信息", "我的信息", "修改个人", "改信息")) fallback.add("BUSINESS");
        boolean knowledgeQuestion = containsAny(normalized, "政策", "流程", "怎么", "什么是", "需要提供", "需要哪些", "哪些信息", "准备什么", "注意事项", "包含什么", "管道维修", "家电维修", "油烟机清洗");
        if (knowledgeQuestion) fallback.add("KNOWLEDGE");
        if (!knowledgeQuestion && containsAny(normalized, "推荐", "附近", "找商家", "找服务", "哪家", "商家列表")) fallback.add("RECOMMENDATION");
        if (fallback.isEmpty()) fallback.add("KNOWLEDGE");
        return List.copyOf(fallback).subList(0, Math.min(3, fallback.size()));
    }

    /** 向后兼容旧调用方，仅取第一个路由。 */
    public String route(String message) { return routes(message).getFirst(); }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
