package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = "diagrams.location=src/test/resources")
class ResourceController_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_FIRST_NAME = "Max";
    private static final String TEST_USER_LAST_NAME = "Mustermann";
    private static final String REGISTERED_USER_MAIL_ADDRESS = "max.mustermann@gmail.com";

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    MockMvc mockMvc;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = AppUser.builder()
            .firstName(TEST_USER_FIRST_NAME)
            .lastName(TEST_USER_LAST_NAME)
            .emailAddress(REGISTERED_USER_MAIL_ADDRESS)
            .status(UserStatus.ACTIVE)
            .build();

        appUserRepository.save(appUser);
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void Should_GetDiagramWithParameters_When_ApiKeyIsCorrect() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "diagrams", "diagram.svg")
            .param("diagramLastModified", "ignored")
            .param("stylingLastModified", "ignored")
            .header("X-API-KEY", appUser.getApiKey().toString()));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void Should_GetDiagramWithoutParameters_When_ApiKeyIsCorrect() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "diagrams", "diagram.svg")
            .header("X-API-KEY", appUser.getApiKey().toString()));

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