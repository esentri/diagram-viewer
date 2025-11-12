package io.domainlifecycles.diagramviewer.rest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.domainlifecycles.diagramviewer.configuration.TestContainersInitializer;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import io.domainlifecycles.diagramviewer.service.RegisteredUserService;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.model.DomainModel;
import io.domainlifecycles.mirror.serialize.api.DomainSerializer;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ExtendWith(TestContainersInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = TestContainersInitializer.class)
@TestPropertySource(properties = "diagrams.location=src/test/resources")
class ResourceController_ITest {

    private static final String REGISTERED_USER_FULL_NAME = "Max Mustermann";
    private static final String REGISTERED_USER_MAIL_ADDRESS = "max.mustermann@gmail.com";

    @Autowired
    RegisteredUserService registeredUserService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    RegisteredUserRepository registeredUserRepository;

    @Autowired
    MockMvc mockMvc;

    private RegisteredUser registeredUser;

    @BeforeEach
    void setUp() {
        registeredUser = RegisteredUser.builder()
            .fullName(REGISTERED_USER_FULL_NAME)
            .emailAddress(REGISTERED_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        registeredUserRepository.save(registeredUser);
        registeredUserService.generateAndSaveApiKey(registeredUser);
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        registeredUserRepository.deleteAll();
    }

    @Test
    void Should_GetDiagramWithParameters_When_ApiKeyIsCorrect() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "diagrams", "diagram.svg")
            .param("diagramLastModified", "ignored")
            .param("stylingLastModified", "ignored")
            .header("X-API-KEY", registeredUser.getApiKey().toString()));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void Should_GetDiagramWithoutParameters_When_ApiKeyIsCorrect() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "diagrams", "diagram.svg")
            .header("X-API-KEY", registeredUser.getApiKey().toString()));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void Should_Return401_When_ApiKeyIsNotCorrect() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "diagrams", "diagram.svg")
            .param("diagramLastModified", "ignored")
            .param("stylingLastModified", "ignored")
            .header("X-API-KEY", UUID.randomUUID().toString()));

        // then
        result.andExpect(status().isUnauthorized());
    }
}