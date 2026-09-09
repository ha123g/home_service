package com.example.home_service_backend.agent.vo;

import java.util.List;

public record AgentCapabilitiesView(
        String orchestrationMode,
        List<String> agents,
        List<String> componentTypes,
        List<String> unsupportedCapabilities) {
}
