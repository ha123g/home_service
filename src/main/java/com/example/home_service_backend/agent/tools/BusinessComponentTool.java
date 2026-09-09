package com.example.home_service_backend.agent.tools;

import com.example.home_service_backend.agent.vo.AgentComponentView;
import com.example.home_service_backend.agent.vo.AgentFormFieldView;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.Map;

/** 与普通业务接口一一对应的固定页面组件 Tool；不执行写操作。 */
@Component
public class BusinessComponentTool {
    @Tool(name = "appointment_form", description = "返回预约申请固定表单；只生成组件，不提交预约")
    public AgentComponentView appointmentForm() {
        return new AgentComponentView(
                "/api/appointments", "POST", true,
                List.of(
                        field("shopId", "商家", "shop", true, true, "必须从真实商家查询结果选择"),
                        field("serviceId", "服务项目", "service", true, true, "必须属于所选商家"),
                        field("region", "服务地区", "region", true, true, "请选择省、市、区县"),
                        field("detail", "服务地址", "map-address", true, true, "可定位或点击地图选择位置，并补充门牌号等详细地址"),
                        field("requirementText", "服务需求", "textarea", true, true, "最长 2048 字"),
                        field("preferredStart", "期望开始时间", "datetime", true, true, null),
                        field("preferredEnd", "期望结束时间", "datetime", false, true, null),
                        field("contactName", "联系人", "text", true, true, null),
                        field("contactPhone", "联系电话", "tel", true, true, "不会写入模型或摘要"),
                        field("idempotencyKey", "幂等键", "hidden", false, false, "由前端生成")),
                Map.of());
    }

    @Tool(name = "payment_confirmation", description = "返回订单支付确认组件；不执行扣款")
    public AgentComponentView paymentConfirmation() {
        return new AgentComponentView(
                "/api/orders/{orderId}/pay", "POST", true,
                List.of(
                        field("orderId", "订单", "order", true, true, "必须选择当前用户可见且待支付的订单"),
                        field("provider", "支付渠道", "hidden", true, true, "当前固定为 STRIPE 沙盒"),
                        field("idempotencyKey", "幂等键", "hidden", false, false, "由前端生成")),
                Map.of("provider", "STRIPE"));
    }

    @Tool(name = "merchant_application_form", description = "返回商家入驻申请固定表单；不执行审批")
    public AgentComponentView merchantApplicationForm() {
        return new AgentComponentView(
                "/api/merchant/applications", "POST", true,
                List.of(
                        field("realName", "真实姓名", "text", true, true, "仅提交业务后端，不进入模型记忆"),
                        field("phone", "联系电话", "tel", true, true, "必须由用户填写"),
                        field("shopName", "商铺名称", "text", true, true, null),
                        field("categoryIds", "服务分类", "category-multiple", true, true, "先选择平台内置标准服务分类"),
                        field("serviceItems", "自定义服务明细", "service-items", true, true, "在所选标准分类下填写服务名称、介绍、起步/参考价、计价单位、时长和标签"),
                        field("tags", "商家标签", "tags", false, true, "可添加多个商家标签，用于首页和推荐展示"),
                        field("intro", "商铺介绍", "textarea", false, true, null),
                        field("serviceRadiusKm", "服务覆盖半径（公里）", "number", true, true, "填写门店周边可服务的公里数（0.1-500）"),
                        field("region", "所在地区", "region", true, true, "省、市、区域联动选择"),
                        field("addressDetail", "详细地址", "map-address", true, true, "通过高德地图选点确认"),
                        field("longitude", "经度", "hidden", true, true, "来自地图选点"),
                        field("latitude", "纬度", "hidden", true, true, "来自地图选点"),
                        field("images", "证明图片", "cos-upload", false, true, "上传至腾讯云 COS")),
                Map.of());
    }

    @Tool(name = "profile_form", description = "返回个人资料修改固定表单")
    public AgentComponentView profileForm() {
        return new AgentComponentView(
                "/api/users/me/profile", "PUT", true,
                List.of(
                        field("nickname", "昵称", "text", false, true, null),
                        field("avatarUrl", "头像", "cos-upload", false, true, "先上传至腾讯云 COS，只提交 Object Key"),
                        field("gender", "性别", "select", false, true, "UNKNOWN/MALE/FEMALE")),
                Map.of());
    }

    @Tool(name = "password_form", description = "返回修改密码固定表单；不读取或保存密码")
    public AgentComponentView passwordForm() {
        return new AgentComponentView(
                "/api/users/me/password", "PUT", true,
                List.of(
                        field("oldPassword", "当前密码", "password", true, true, "不得发送给模型或写入记忆"),
                        field("newPassword", "新密码", "password", true, true, "不得发送给模型或写入记忆")),
                Map.of());
    }

    @Tool(name = "business_help", description = "说明当前对话可以查询和办理的业务；不读取数据、不提交写操作")
    public AgentComponentView businessHelp() {
        return new AgentComponentView(
                "/api/agent/capabilities", "GET", false, List.of(), Map.of());
    }

    @Tool(name = "business_navigation", description = "返回业务页面固定入口，不执行任何写操作")
    public AgentComponentView navigation(String endpoint) {
        return new AgentComponentView(endpoint, "GET", false, List.of(), Map.of());
    }

    private AgentFormFieldView field(
            String name, String label, String inputType, boolean required,
            boolean confirmationRequired, String description) {
        return new AgentFormFieldView(
                name, label, inputType, required, confirmationRequired, description);
    }
}
