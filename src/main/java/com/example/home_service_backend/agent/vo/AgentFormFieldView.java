package com.example.home_service_backend.agent.vo;

public record AgentFormFieldView(
        String name,
        String label,
        String inputType,
        boolean required,
        boolean userConfirmationRequired,
        String description) {
}
