package com.example.home_service_backend.agent.skills.knowledge;

import com.example.home_service_backend.agent.dto.internal.AgentContext;
import com.example.home_service_backend.agent.dto.internal.AgentResult;
import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.agent.prompts.AgentPrompts;
import com.example.home_service_backend.agent.rag.RagContextAssembler;
import com.example.home_service_backend.agent.rag.RagDocument;
import com.example.home_service_backend.agent.tools.KnowledgeRetrievalTool;
import com.example.home_service_backend.agent.skills.AgentSkill;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class KnowledgeAgent implements AgentSkill {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeAgent.class);
    public static final String MODEL_NAME = "qwen3.8-27b";
    private final KnowledgeRetrievalTool retrievalTool;
    private final RagContextAssembler contextAssembler;
    private final AgentModelFactory modelFactory;

    public KnowledgeAgent(
            KnowledgeRetrievalTool retrievalTool,
            RagContextAssembler contextAssembler,
            AgentModelFactory modelFactory) {
        this.retrievalTool = retrievalTool;
        this.contextAssembler = contextAssembler;
        this.modelFactory = modelFactory;
    }

    @Override
    public AgentResult execute(AgentContext context) {
        if (context.message() != null && context.message().trim().matches("(?i)(你好|您好|嗨|hello|hi)[！!。.! ]*")) {
            return new AgentResult("你好，我可以帮你了解家政服务、查找营业商家，或协助填写预约等业务表单。", "TEXT", "knowledge.v1", null, List.of(), "KNOWLEDGE", 0, 0, 0);
        }
        List<RagDocument> documents = retrievalTool.search(context.message(), "knowledge");
        if (documents.isEmpty()) {
            return new AgentResult(
                    "当前知识库没有可靠依据，请补充问题或联系平台人工客服。",
                    "TEXT", "knowledge.v1", null, List.of(), "KNOWLEDGE", 0, 0, 0);
        }

        String knowledge = contextAssembler.assemble(documents);
        try {
            var result = modelFactory.create(MODEL_NAME).chat(
                    AgentPrompts.KNOWLEDGE_V1 + "\n知识：\n" + knowledge,
                    context.message(),
                    1000);
            return new AgentResult(
                    result.content(), "TEXT", "knowledge.v1", null, documents, "KNOWLEDGE",
                    result.inputTokens(), result.outputTokens(), result.latencyMs());
        } catch (RuntimeException exception) {
            log.warn("[AI] 知识问答模型调用失败；会话编号={}，原因={}",
                    context.sessionId(), exception.getClass().getSimpleName());
            RagDocument first = documents.getFirst();
            // 模型不可用时也不能把整段/整篇 FAQ 原文直接返回给用户；只提供受限摘要，
            // 由上层 Summary Agent 在可用时继续整理，引用仍保留在结构化结果中。
            String fallback = "已找到《" + safe(first.title()) + "》中的相关资料，但当前暂时无法生成简洁回答，请稍后重试。";
            return new AgentResult(
                    fallback, "TEXT", "knowledge.v1", null, documents, "KNOWLEDGE", 0, 0, 0);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "未命名资料" : value;
    }
}
