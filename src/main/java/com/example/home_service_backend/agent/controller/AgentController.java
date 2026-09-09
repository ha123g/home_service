package com.example.home_service_backend.agent.controller;

import com.example.home_service_backend.agent.dto.request.AgentChatRequest;
import com.example.home_service_backend.agent.service.AgentApplicationService;
import com.example.home_service_backend.agent.vo.AgentCapabilitiesView;
import com.example.home_service_backend.agent.vo.AgentResponse;
import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.AGENT_PREFIX)
public class AgentController {
    private final AgentApplicationService agentService;

    public AgentController(AgentApplicationService agentService) {
        this.agentService = agentService;
    }

    /**
     * 模型聊天
     */
    @PostMapping("/chat")
    public ResponseEntity<ResultData<AgentResponse>> chat(
            @Valid @RequestBody AgentChatRequest request) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(agentService.chat(request)));
    }

    /**
     * 获取模型能力
     */
    @GetMapping("/capabilities")
    public ResponseEntity<ResultData<AgentCapabilitiesView>> capabilities() {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(agentService.capabilities()));
    }
}
