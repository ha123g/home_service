package com.example.home_service_backend.agent.model;

import com.example.home_service_backend.agent.config.AgentModelProperties;
import com.example.home_service_backend.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** 模型工厂：Agent 只声明用途并 create，不直接依赖供应商 SDK。 */
@Component
public class AgentModelFactory {
    private static final Logger log = LoggerFactory.getLogger(AgentModelFactory.class);
    // 默认模型温度为 0.2，最大输出令牌数为 1200。
    private static final ModelProfile DEFAULT_PROFILE = new ModelProfile(0.2D, 1200);
    // 模型温度和最大输出令牌数的映射。
    private static final Map<String, ModelProfile> MODEL_PROFILES = Map.of(
            "qwen-turbo", new ModelProfile(0.0D, 300),
            "qwen-plus", new ModelProfile(0.1D, 1200),
            "qwen-max", new ModelProfile(0.1D, 1600),
            "gpt-4o-mini", new ModelProfile(0.2D, 1200),
            "gpt-4.1-mini", new ModelProfile(0.1D, 1200));
    // 配置属性。
    private final AgentModelProperties properties;
    private final Map<ModelKey, ChatModelClient> cache = new HashMap<>();
    private EmbeddingClient embeddingClient;

    public AgentModelFactory(AgentModelProperties properties) {
        this.properties = properties;
    }

    /**
     * 按智能体在代码中声明的模型名称创建客户端。
     * 端点和密钥仍由配置提供。
     */
    public synchronized ChatModelClient create(String modelName) {
        return create(modelName, null, null);
    }

    /** 创建客户端并允许该智能体显式覆盖温度和输出上限。null 表示使用工厂默认值。 */
    public synchronized ChatModelClient create(String modelName, Double temperature, Integer maxOutputTokens) {
        try {
            if (modelName == null || modelName.isBlank()) {
                throw new BusinessException("503", "未指定 Agent 模型");
            }
            ensureSupportedProvider();
            String key = modelName.trim();
            double resolvedTemperature = temperature == null
                    ? defaultTemperature(key) : clampTemperature(temperature);
            int resolvedMaxTokens = maxOutputTokens == null
                    ? defaultMaxOutputTokens(key) : Math.max(1, maxOutputTokens);
            ModelKey cacheKey = new ModelKey(key, resolvedTemperature, resolvedMaxTokens);
            ChatModelClient client = cache.computeIfAbsent(cacheKey,
                    ignored -> new OpenAiCompatibleChatModel(properties, key,
                            resolvedTemperature, resolvedMaxTokens));
            log.info("[AI] modelFactory.create 调用成功；模型={}，温度={}，最大输出令牌={}", key, resolvedTemperature, resolvedMaxTokens);
            return client;
        } catch (RuntimeException exception) {
            log.warn("[AI] modelFactory.create 调用失败；模型={}，原因={}", modelName, exception.getClass().getSimpleName());
            throw exception;
        }
    }

    /**
     * 创建嵌入客户端。
     * 端点和密钥仍由配置提供。
     */
    public synchronized EmbeddingClient createEmbedding() {
        try {
            ensureSupportedProvider();
            if (embeddingClient == null) {
                embeddingClient = new OpenAiCompatibleEmbeddingClient(properties);
            }
            log.info("[AI] modelFactory.createEmbedding 调用成功；模型={}", properties.embeddingModel());
            return embeddingClient;
        } catch (RuntimeException exception) {
            log.warn("[AI] modelFactory.createEmbedding 调用失败；模型={}，原因={}",
                    properties.embeddingModel(), exception.getClass().getSimpleName());
            throw exception;
        }
    }

    private double defaultTemperature(String model) {
        return profile(model).temperature();
    }

    private int defaultMaxOutputTokens(String model) {
        return profile(model).maxOutputTokens();
    }

    private ModelProfile profile(String model) {
        return MODEL_PROFILES.getOrDefault(model.toLowerCase(java.util.Locale.ROOT), DEFAULT_PROFILE);
    }

    /**
     * 将温度值限制在有效范围内。
     * 温度值必须在 0.0 到 2.0 之间。
     */
    private double clampTemperature(double value) {
        return Math.max(0D, Math.min(2D, value));
    }

    /**
     * 确保配置的提供者受支持。
     * 目前仅支持 OpenAI 兼容的提供者和阿里云的 Dashscope。
     */
    private void ensureSupportedProvider() {
        String provider = properties.provider();
        if (provider != null
                && !provider.isBlank()
                && !"openai-compatible".equalsIgnoreCase(provider)
                && !"dashscope".equalsIgnoreCase(provider)
                && !"ollama".equalsIgnoreCase(provider)) {
            throw new BusinessException("503", "暂不支持的模型供应商: " + provider);
        }
    }

    private record ModelKey(String model, double temperature, int maxOutputTokens) {
    }

    private record ModelProfile(double temperature, int maxOutputTokens) {
    }
}
