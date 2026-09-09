package com.example.home_service_backend.agent;

import com.example.home_service_backend.agent.common.utils.AgentSummarySanitizerUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentSummarySanitizerTests {
    @Test
    void fallbackDoesNotRetainOriginalSensitiveText() {
        AgentSummarySanitizerUtil sanitizer = new AgentSummarySanitizerUtil();
        String summary = sanitizer.fallbackSummary("预约，电话 13800138000，地址是测试路 1 号");

        assertThat(summary).contains("业务办理");
        assertThat(summary).doesNotContain("13800138000", "测试路", "1 号");
    }
}
