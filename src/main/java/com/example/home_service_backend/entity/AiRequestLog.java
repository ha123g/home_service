package com.example.home_service_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_request_log")
public class AiRequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, length = 128, unique = true)
    private String requestId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "route_type", nullable = false, length = 32)
    private String routeType;
    @Column(name = "detail_level", nullable = false, length = 16)
    private String detailLevel;
    @Column(name = "agent_name", length = 64)
    private String agentName;
    @Column(name = "component_type", length = 64)
    private String componentType;
    @Column(name = "model_name", length = 128)
    private String modelName;
    @Column(name = "input_tokens")
    private Integer inputTokens;
    @Column(name = "output_tokens")
    private Integer outputTokens;
    @Column(name = "retrieval_count")
    private Integer retrievalCount;
    @Column(name = "tool_call_count", nullable = false)
    private Integer toolCallCount;
    @Column(name = "latency_ms")
    private Integer latencyMs;
    @Column(name = "status", nullable = false, length = 16)
    private String status;
    @Column(name = "error_code", length = 64)
    private String errorCode;
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;

    public Long getId() { return id; }
    public String getRequestId() { return requestId; }
    public Long getUserId() { return userId; }
    public String getRouteType() { return routeType; }
    public String getAgentName() { return agentName; }
    public String getModelName() { return modelName; }
    public Integer getInputTokens() { return inputTokens; }
    public Integer getOutputTokens() { return outputTokens; }
    public Integer getLatencyMs() { return latencyMs; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedTime() { return createdTime; }

    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setRouteType(String routeType) { this.routeType = routeType; }
    public void setDetailLevel(String detailLevel) { this.detailLevel = detailLevel; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public void setComponentType(String componentType) { this.componentType = componentType; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }
    public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }
    public void setRetrievalCount(Integer retrievalCount) { this.retrievalCount = retrievalCount; }
    public void setToolCallCount(Integer toolCallCount) { this.toolCallCount = toolCallCount; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }
    public void setStatus(String status) { this.status = status; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
}
