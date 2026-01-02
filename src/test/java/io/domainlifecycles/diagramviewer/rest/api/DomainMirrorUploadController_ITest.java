package io.domainlifecycles.diagramviewer.rest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.service.AppUserService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DomainMirrorUploadController_ITest extends BaseIntegrationTest {

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
    private Project project;

    @BeforeEach
    void setUp() {
        appUser = AppUser.builder()
            .firstName(TEST_USER_FIRST_NAME)
            .lastName(TEST_USER_LAST_NAME)
            .emailAddress(REGISTERED_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        appUserRepository.save(appUser);

        project = setUpProject();
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void Should_UploadDomainMirror_When_ApiKeyIsCorrect() throws Exception {

        // given
        String jsonBody = getDomainMirrorJson();

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
            .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", appUser.getApiKey().toString())
            .content(jsonBody));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void Should_Return401_When_ApiKeyIsNotCorrect() throws Exception {

        // given
        String jsonBody = getDomainMirrorJson();

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-API-KEY", UUID.randomUUID().toString())
            .content(jsonBody));

        // then
        result.andExpect(status().isUnauthorized());
    }

    private String getDomainMirrorJson() throws JsonProcessingException {
        DomainMirror domainMirror = new DomainModel(Map.of(), "test.package");

        ObjectMapper mapper = new ObjectMapper();
        DomainSerializer serializer = new JacksonDomainSerializer(false);
        String jsonObject = serializer.serialize(domainMirror);
        return mapper.writeValueAsString(jsonObject);
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .diagrams(new HashSet<>())
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .creator(appUser)
            .build();

        return projectRepository.save(project);
    }
}