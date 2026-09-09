package com.example.home_service_backend.agent.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({
        AgentModelProperties.class,
        AgentRagProperties.class,
        AgentMemoryProperties.class,
        AgentPersistenceProperties.class
})
public class AgentConfig {
}
