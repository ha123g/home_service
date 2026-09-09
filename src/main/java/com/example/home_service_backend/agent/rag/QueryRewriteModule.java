package com.example.home_service_backend.agent.rag;

import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.agent.prompts.AgentPrompts;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 将口语问题改写成一次独立检索查询；失败时保留原查询。 */
@Component
public class QueryRewriteModule {
    private static final Logger log = LoggerFactory.getLogger(QueryRewriteModule.class);
    private final AgentModelFactory modelFactory;

    public QueryRewriteModule(AgentModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    public String rewrite(String query) {
        try {
            String rewritten = modelFactory.create("qwen-plus", 0.0D, 128)
                    .chat(AgentPrompts.QUERY_REWRITE_V1,
                            query, 128)
                    .content()
                    .trim();
            return rewritten.isBlank() ? query : rewritten;
        } catch (RuntimeException exception) {
            log.warn("[AI] RAG 查询改写模型调用失败；原因={}", exception.getClass().getSimpleName());
            return query;
        }
    }
}
