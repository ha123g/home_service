package com.example.home_service_backend.agent.model;

/** 五类 Agent 的职责标签，仅用于文档、审计和能力矩阵；模型名称由各 Agent 显式声明。 */
public enum AgentModelPurpose {
    ROUTER,
    KNOWLEDGE,
    RECOMMENDATION,
    BUSINESS,
    SUMMARY
}
