package com.example.home_service_backend.agent;

import com.example.home_service_backend.agent.dto.internal.AgentContext;
import com.example.home_service_backend.agent.skills.business.BusinessAgent;
import com.example.home_service_backend.agent.tools.BusinessComponentTool;
import com.example.home_service_backend.agent.vo.AgentComponentView;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessAgentTests {
    private final BusinessAgent agent = new BusinessAgent(null, new BusinessComponentTool());

    @Test
    void returnsFixedAppointmentFormWhenModelIsUnavailable() {
        var result = agent.execute(new AgentContext(1L, "session", "我要预约保洁", null));

        assertThat(result.componentType()).isEqualTo("APPOINTMENT_FORM");
        assertThat(result.data()).isInstanceOf(AgentComponentView.class);
        AgentComponentView component = (AgentComponentView) result.data();
        assertThat(component.endpoint()).isEqualTo("/api/appointments");
        assertThat(component.confirmationRequired()).isTrue();
    }

    @Test
    void passwordComponentNeverPrefillsPassword() {
        var result = agent.execute(new AgentContext(1L, "session", "我要修改密码", null));

        assertThat(result.componentType()).isEqualTo("PASSWORD_FORM");
        AgentComponentView component = (AgentComponentView) result.data();
        assertThat(component.defaults()).isEmpty();
        assertThat(component.fields()).extracting("name")
                .containsExactly("oldPassword", "newPassword");
    }
}
