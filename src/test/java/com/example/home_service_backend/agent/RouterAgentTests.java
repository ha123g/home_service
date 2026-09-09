package com.example.home_service_backend.agent;

import com.example.home_service_backend.agent.model.AgentModelFactory;
import com.example.home_service_backend.agent.skills.router.RouterAgent;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RouterAgentTests {
    @Test
    void routesKnownBusinessIntentsWithoutModelConfiguration() {
        RouterAgent router = new RouterAgent(null);
        assertThat(router.route("我想预约保洁")).isEqualTo("BUSINESS");
        assertThat(router.route("附近有哪些家政商家")).isEqualTo("RECOMMENDATION");
    }
}
