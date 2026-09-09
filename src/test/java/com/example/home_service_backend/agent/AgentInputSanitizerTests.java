package com.example.home_service_backend.agent;

import com.example.home_service_backend.agent.common.utils.AgentInputSanitizerUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentInputSanitizerTests {
    @Test
    void removesCommonSecretsBeforeModelCalls() {
        String input = "预约，电话13800138000，密码是p@ssword，地址：测试路1号，邮箱a@b.com";

        String sanitized = new AgentInputSanitizerUtil().sanitizeForModel(input);

        assertThat(sanitized)
                .contains("[REDACTED_PHONE]", "[REDACTED_SECRET]", "[REDACTED_ADDRESS]")
                .doesNotContain("13800138000", "p@ssword", "测试路1号", "a@b.com");
    }
}
