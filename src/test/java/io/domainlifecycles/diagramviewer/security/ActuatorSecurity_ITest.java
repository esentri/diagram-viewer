package io.domainlifecycles.diagramviewer.security;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Only the health check of the actuator is reachable without signing in.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorSecurity_ITest extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void Should_AnswerHealthCheck_WithoutSigningIn() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void Should_RequireSigningIn_ForOtherActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator"))
            .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/actuator/loggers"))
            .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/actuator/env"))
            .andExpect(status().is3xxRedirection());
    }
}
