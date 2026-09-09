package com.example.home_service_backend.agent.skills;

import com.example.home_service_backend.agent.dto.internal.AgentContext;
import com.example.home_service_backend.agent.dto.internal.AgentResult;

public interface AgentSkill {
    AgentResult execute(AgentContext context);
}
