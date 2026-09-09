package com.example.home_service_backend.agent.vo;

import java.util.List;
import java.util.Map;

/** 前端白名单组件契约；后端只返回固定类型和版本，不生成页面代码。 */
public record AgentComponentView(
        String endpoint,
        String method,
        boolean confirmationRequired,
        List<AgentFormFieldView> fields,
        Map<String, Object> defaults) {
}
