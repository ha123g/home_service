package com.example.home_service_backend.agent.vo;
import com.example.home_service_backend.agent.rag.RagDocument;
import java.util.List;
public record AgentResponse(String route, String answer, String componentType, String schemaVersion,
                            Object data, List<RagDocument> citations, String requestId) {}
