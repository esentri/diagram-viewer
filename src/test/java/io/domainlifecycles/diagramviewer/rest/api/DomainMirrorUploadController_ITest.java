package io.domainlifecycles.diagramviewer.rest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
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
    private Project project;

    @BeforeEach
    void setUp() {
        registeredUser = RegisteredUser.builder()
            .fullName(REGISTERED_USER_FULL_NAME)
            .emailAddress(REGISTERED_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        registeredUserRepository.save(registeredUser);

        project = setUpProject();
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        registeredUserRepository.deleteAll();
    }

    @Test
    void Should_UploadDomainMirror_When_ApiKeyIsCorrect() throws Exception {

        // given
        String jsonBody = getDomainMirrorJson();

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
            .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", registeredUser.getApiKey().toString())
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
            .assignedRegisteredUsers(new HashSet<>(Set.of(registeredUser)))
            .assignedInvitedUsers(new HashSet<>())
            .creator(registeredUser)
            .build();

        return projectRepository.save(project);
    }
}