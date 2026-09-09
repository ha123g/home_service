package com.example.home_service_backend.agent.common.utils;

import org.springframework.stereotype.Component;

/** 模型不可用时生成不包含用户原文和隐私字段的最小摘要。 */
@Component
public class AgentSummarySanitizerUtil {
    public String fallbackSummary(String message) {
        String intent = "知识咨询";
        if (containsAny(message, "推荐", "附近", "商家", "服务")) {
            intent = "商家或服务推荐";
        } else if (containsAny(message, "预约", "订单", "支付", "资料", "入驻")) {
            intent = "业务办理";
        }
        return "最近意图：" + intent + "；已确认槽位：无；待办：等待用户在受控组件中补充或确认信息。";
    }

    private boolean containsAny(String value, String... candidates) {
        if (value == null) {
            return false;
        }
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
