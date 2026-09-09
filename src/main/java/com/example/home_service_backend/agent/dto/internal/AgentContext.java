package com.example.home_service_backend.agent.dto.internal;

import java.math.BigDecimal;

/** Agent 内部上下文，不是 HTTP 请求/响应，也不直接暴露给客户端。 */
public record AgentContext(
        Long userId,
        String sessionId,
        String message,
        String memorySummary,
        BigDecimal latitude,
        BigDecimal longitude) {
    public AgentContext(Long userId, String sessionId, String message, String memorySummary) {
        this(userId, sessionId, message, memorySummary, null, null);
    }
}
