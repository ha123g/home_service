package com.example.home_service_backend.agent.skills.business;

import com.example.home_service_backend.agent.dto.internal.AgentContext;
import com.example.home_service_backend.agent.dto.internal.AgentResult;
import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.agent.model.ChatModelClient;
import com.example.home_service_backend.agent.prompts.AgentPrompts;
import com.example.home_service_backend.agent.skills.AgentSkill;
import com.example.home_service_backend.agent.tools.BusinessComponentTool;
import com.example.home_service_backend.agent.tools.BusinessQueryTool;
import com.example.home_service_backend.agent.vo.AgentComponentView;
import com.example.home_service_backend.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 业务智能体：模型通过 Function Calling 选择一个白名单 Tool，应用层再执行权限校验后的
 * 查询或返回固定表单。模型不能直接写数据库，也不能伪造用户、资源编号或业务状态。
 */
@Component
public class BusinessAgent implements AgentSkill {
    private static final Logger log = LoggerFactory.getLogger(BusinessAgent.class);
    public static final String MODEL_NAME = "qwen3.8-27b";

    private static final Map<String, BusinessIntent> TOOL_INTENTS = Map.ofEntries(
            Map.entry("query_my_orders", BusinessIntent.ORDER_LIST),
            Map.entry("query_my_appointments", BusinessIntent.APPOINTMENT_LIST),
            Map.entry("query_my_merchant_application", BusinessIntent.APPLICATION_STATUS),
            Map.entry("appointment_form", BusinessIntent.APPOINTMENT_CREATE),
            Map.entry("merchant_application_form", BusinessIntent.MERCHANT_APPLICATION),
            Map.entry("profile_form", BusinessIntent.PROFILE_UPDATE),
            Map.entry("password_form", BusinessIntent.PASSWORD_CHANGE),
            Map.entry("payment_confirmation", BusinessIntent.PAYMENT),
            Map.entry("business_help", BusinessIntent.HELP));

    /** 名称与 agent/tools 上的 @Tool 契约保持一致，由模型按语义选择。 */
    private static final List<ChatModelClient.ToolDefinition> BUSINESS_TOOLS = List.of(
            tool("query_my_orders", "查询当前用户已有订单。询问有没有订单、订单消息、订单列表或订单进度时必须使用；不能改成新建预约"),
            tool("query_my_appointments", "查询当前用户已有预约。询问有没有预约、预约消息、预约记录或预约进度时必须使用；不能打开新预约表单"),
            tool("query_my_merchant_application", "查询当前用户已有商家入驻申请及审核状态；询问申请状态、审核进度时使用"),
            tool("appointment_form", "创建新的预约申请表单。仅当用户明确要预约某项服务、提交预约或下单找服务时使用"),
            tool("merchant_application_form", "创建商家入驻申请表单。仅当用户明确要申请开店或入驻时使用"),
            tool("profile_form", "修改当前用户昵称、头像或性别的表单"),
            tool("password_form", "修改当前用户登录密码的表单。用户说修改密码、更换密码、密码设置时使用"),
            tool("payment_confirmation", "支付当前用户已有待支付订单的确认组件"),
            tool("business_help", "用户只是在询问助手支持哪些业务，且没有明确查询或办理目标时使用"));

    private final AgentModelFactory modelFactory;
    private final BusinessComponentTool componentTool;
    private final BusinessQueryTool queryTool;

    @Autowired
    public BusinessAgent(AgentModelFactory modelFactory,
                         BusinessComponentTool componentTool,
                         BusinessQueryTool queryTool) {
        this.modelFactory = modelFactory;
        this.componentTool = componentTool;
        this.queryTool = queryTool;
    }

    /** 保留轻量单元测试及模型不可用测试的构造方式。 */
    public BusinessAgent(AgentModelFactory modelFactory, BusinessComponentTool componentTool) {
        this(modelFactory, componentTool, null);
    }

    @Override
    public AgentResult execute(AgentContext context) {
        Selection selection = selectTool(context);
        return executeSelected(selection);
    }

    private Selection selectTool(AgentContext context) {
        // 首先进行业务状态查询和明确写意图的兜底。
        BusinessIntent guardedIntent = guardedIntent(context.message());
        if (guardedIntent != null) {
            // 业务状态查询和明确写意图属于不可歧义的安全边界，不能交给模型自由改写。
            return new Selection(guardedIntent, 0, 0, 0, true);
        }
        // 尝试使用模型进行 Function Calling
        if (modelFactory != null) {
            try {
                String prompt = "本轮用户请求：" + context.message()
                        + (context.memorySummary() == null || context.memorySummary().isBlank()
                        ? "" : "\n已脱敏的短期上下文：" + context.memorySummary());
                var result = modelFactory.create(MODEL_NAME, 0.0D, 240)
                        .callTools(AgentPrompts.BUSINESS_FUNCTION_CALLING_V2, prompt,
                                BUSINESS_TOOLS, 240);
                for (var call : result.toolCalls()) {
                    BusinessIntent intent = TOOL_INTENTS.get(call.name());
                    if (intent != null) {
                        return new Selection(intent, result.inputTokens(), result.outputTokens(), result.latencyMs(), false);
                    }
                }
            } catch (RuntimeException exception) {
                log.warn("[AI] 业务智能体调用工具失败，使用规则兜底；会话编号={}，原因={}",
                        context.sessionId(), exception.getClass().getSimpleName());
            }
        }
        // 只有模型不可用或 Function Calling 返回非法 Tool 时才启用关键词兜底。
        return new Selection(fallbackRecognize(context.message()), 0, 0, 0, true);
    }

    // 业务状态查询和明确写意图属于不可歧义的安全边界，不能交给模型自由改写。
    private BusinessIntent guardedIntent(String message) {
        String value = message == null ? "" : message.trim();
        if (containsAny(value, "入驻申请状况", "入驻申请情况", "入驻审核", "申请状态", "审核进度", "入驻情况", "申请进展", "审核结果")) {
            return BusinessIntent.APPLICATION_STATUS;
        }
        if (containsAny(value, "我要申请入驻", "我想申请入驻", "申请入驻", "我要开店", "我想开店", "开店申请")) {
            return BusinessIntent.MERCHANT_APPLICATION;
        }
        if (containsAny(value, "我要预约", "我想预约", "帮我预约", "预约服务", "我要下单", "我想下单")) {
            return BusinessIntent.APPOINTMENT_CREATE;
        }
        return null;
    }

    private AgentResult executeSelected(Selection selection) {
        return switch (selection.intent()) {
            case ORDER_LIST -> queryOrders(selection);
            case APPOINTMENT_LIST -> queryAppointments(selection);
            case APPLICATION_STATUS -> queryApplication(selection);
            case PAYMENT -> componentResult(selection, "PAYMENT_CONFIRM",
                    componentTool.paymentConfirmation(), "已准备待支付订单确认组件，提交前必须由用户确认订单和金额。");
            case APPOINTMENT_CREATE -> componentResult(selection, "APPOINTMENT_FORM",
                    componentTool.appointmentForm(), "已准备新预约表单，需要用户选择真实商家和该商家的服务并确认提交。");
            case MERCHANT_APPLICATION -> componentResult(selection, "MERCHANT_APPLICATION_FORM",
                    componentTool.merchantApplicationForm(), "已准备商家入驻表单，需要用户确认服务项目、位置及申请资料后提交。");
            case PROFILE_UPDATE -> componentResult(selection, "PROFILE_FORM",
                    componentTool.profileForm(), "已准备个人资料表单，用户可修改昵称、头像或性别并确认保存。");
            case PASSWORD_CHANGE -> componentResult(selection, "PASSWORD_FORM",
                    componentTool.passwordForm(), "已准备密码修改表单；密码字段只提交业务接口，不发送给模型或写入记忆。");
            case HELP -> componentResult(selection, "BUSINESS_HELP",
                    componentTool.businessHelp(), "当前对话支持查询订单、预约和入驻审核，也支持预约、支付、资料和密码表单办理。");
        };
    }

    private AgentResult queryOrders(Selection selection) {
        if (queryTool == null) return fallbackResult(selection, "目前无法查询订单，请稍后重试。");
        var page = queryTool.myOrders();
        String facts = page.items().isEmpty()
                ? "当前没有订单。"
                : "你有 " + page.total() + " 笔订单："
                + page.items().stream().limit(5)
                .map(order -> safeTitle(order.serviceTitleSnapshot(), order.orderNo())
                        + "，状态=" + orderStatusText(order.status()))
                .collect(java.util.stream.Collectors.joining("；")) + "。";
        return result(selection, facts, "TEXT", page);
    }

    private AgentResult queryAppointments(Selection selection) {
        if (queryTool == null) return fallbackResult(selection, "目前无法查询预约，请稍后重试。");
        var page = queryTool.myAppointments();
        String facts = page.items().isEmpty()
                ? "当前没有预约记录。"
                : "你有 " + page.total() + " 条预约记录："
                + page.items().stream().limit(5)
                .map(item -> safeTitle(item.serviceTitle(), item.requestNo())
                        + "，状态=" + appointmentStatusText(item.status()))
                .collect(java.util.stream.Collectors.joining("；")) + "。";
        return result(selection, facts, "TEXT", page);
    }

    private AgentResult queryApplication(Selection selection) {
        if (queryTool == null) return fallbackResult(selection, "目前无法查询入驻申请，请稍后重试。");
        try {
            var application = queryTool.myMerchantApplication();
            String facts = "你的入驻申请编号为 " + application.applyNo()
                    + "，当前状态为 " + statusText(application.status())
                    + (application.reviewRemark() == null || application.reviewRemark().isBlank()
                    ? "。" : "；审核备注=" + application.reviewRemark() + "。");
            return result(selection, facts, "TEXT", application);
        } catch (BusinessException exception) {
            if ("404".equals(exception.getCode())) {
                return result(selection, "暂未找到你的入驻申请记录。",
                        "TEXT", Map.of("kind", "MERCHANT_APPLICATION", "total", 0));
            }
            throw exception;
        }
    }

    private AgentResult componentResult(Selection selection, String componentType,
                                        AgentComponentView component, String facts) {
        return result(selection, facts, componentType, component);
    }

    private AgentResult fallbackResult(Selection selection, String answer) {
        return result(selection, answer, "TEXT", Map.of("fallback", true));
    }

    private AgentResult result(Selection selection, String answer, String componentType, Object data) {
        return new AgentResult(answer, componentType, "business.v2", data, null, "BUSINESS",
                selection.inputTokens(), selection.outputTokens(), selection.latencyMs());
    }

    private BusinessIntent fallbackRecognize(String message) {
        String value = message == null ? "" : message;
        if (containsAny(value, "修改密码", "更改密码", "换密码", "密码设置")) return BusinessIntent.PASSWORD_CHANGE;
        if (containsAny(value, "有没有订单", "有订单吗", "没有订单", "订单消息", "订单记录", "查看订单", "我的订单")) return BusinessIntent.ORDER_LIST;
        if (containsAny(value, "有没有预约", "有预约吗", "没有预约", "预约消息", "预约记录", "查看预约", "我的预约")) return BusinessIntent.APPOINTMENT_LIST;
        if (containsAny(value, "入驻审核", "申请状态", "审核进度", "入驻情况", "入驻申请状况", "入驻申请情况", "申请进展", "审核结果")) return BusinessIntent.APPLICATION_STATUS;
        if (containsAny(value, "支付", "付款")) return BusinessIntent.PAYMENT;
        if (containsAny(value, "个人资料", "个人信息", "修改昵称", "换头像")) return BusinessIntent.PROFILE_UPDATE;
        if (containsAny(value, "入驻", "申请商家", "开店")) return BusinessIntent.MERCHANT_APPLICATION;
        if (containsAny(value, "预约", "下单")) return BusinessIntent.APPOINTMENT_CREATE;
        return BusinessIntent.HELP;
    }

    private static ChatModelClient.ToolDefinition tool(String name, String description) {
        return ChatModelClient.ToolDefinition.withoutArguments(name, description);
    }

    private String safeTitle(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) return preferred;
        return fallback == null || fallback.isBlank() ? "服务记录" : fallback;
    }

    private String statusText(String status) {
        return switch (status) {
            case "PENDING" -> "申请中";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已驳回";
            case "CANCELLED" -> "已取消";
            default -> "处理中";
        };
    }

    private String orderStatusText(String status) {
        return switch (status) {
            case "PENDING_PUBLISH" -> "待发布";
            case "PENDING_PAYMENT" -> "待支付";
            case "PAID" -> "已支付";
            case "CONFIRMED" -> "已确认";
            case "IN_SERVICE" -> "服务中";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "CLOSED" -> "已关闭";
            default -> "处理中";
        };
    }

    private String appointmentStatusText(String status) {
        return switch (status) {
            case "PENDING_PLATFORM" -> "申请中";
            case "CONTACTING" -> "联系中";
            case "WORKER_ARRANGED" -> "已安排";
            case "ORDER_PENDING" -> "待订单发布";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "CLOSED" -> "已关闭";
            default -> "处理中";
        };
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) return true;
        }
        return false;
    }

    private record Selection(BusinessIntent intent, int inputTokens, int outputTokens,
                             long latencyMs, boolean fallback) {}

    private enum BusinessIntent {
        APPOINTMENT_CREATE,
        APPOINTMENT_LIST,
        ORDER_LIST,
        PAYMENT,
        MERCHANT_APPLICATION,
        PROFILE_UPDATE,
        PASSWORD_CHANGE,
        APPLICATION_STATUS,
        HELP
    }
}
