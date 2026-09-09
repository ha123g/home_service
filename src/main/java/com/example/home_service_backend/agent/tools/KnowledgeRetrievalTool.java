package com.example.home_service_backend.agent.tools;

import com.example.home_service_backend.agent.rag.ModularRagService;
import com.example.home_service_backend.agent.rag.RagDocument;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/** 只读知识检索 Tool；不允许写入业务数据。 */
@Component
public class KnowledgeRetrievalTool {
    private final ModularRagService ragService;

    public KnowledgeRetrievalTool(ModularRagService ragService) {
        this.ragService = ragService;
    }

    @Tool(name = "knowledge_search", description = "检索版本有效且可追溯的家政知识文档，只读")
    public List<RagDocument> search(
            @ToolParam(description = "用户的知识问题") String query,
            @ToolParam(description = "知识分类；未指定时使用 knowledge") String category) {
        return ragService.search(query, category);
    }
}
