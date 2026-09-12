package io.domainlifecycles.diagramviewer.rest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.service.AppUserService;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.model.DomainModel;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import io.domainlifecycles.mirror.serialize.jackson2.JacksonDomainSerializer;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.serialize.DomainCallsSerializer;
import io.domainlifecycles.staticanalysis.serialize.jackson2.JacksonDomainCallsSerializer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.assertj.core.api.Assertions.assertThat;
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
    ProjectDomainMirrorRepository projectDomainMirrorRepository;

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
            .status(UserStatus.ACTIVE)
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

    @Test
    void Should_UploadDomainMirrorAndDomainCalls_When_BothAreProvided() throws Exception {

        // given
        String jsonBody = getDomainMirrorAndDomainCallsJson();

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-API-KEY", appUser.getApiKey().toString())
            .content(jsonBody));

        // then
        result.andExpect(status().isOk());
        // the endpoint looks up (and, if needed, creates) the project by its cleaned name (dots/dashes
        // replaced with underscores), same as setUpProject()'s raw "project-1.0.0.jar" name would resolve to
        String cleanedProjectName = project.getName().replaceAll("[.-]", "_");
        Project updatedProject = projectRepository.findByName(cleanedProjectName).orElseThrow();
        ProjectDomainMirror stored = projectDomainMirrorRepository.findByProjectId(updatedProject.getId()).orElseThrow();
        assertThat(stored.getDomainCalls()).isNotBlank();
    }

    @Test
    void Should_UploadDomainMirror_When_BodyIsGzipCompressed() throws Exception {

        // given
        byte[] gzippedBody = gzip(getDomainMirrorJson());

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-API-KEY", appUser.getApiKey().toString())
            .header("Content-Encoding", "gzip")
            .content(gzippedBody));

        // then
        result.andExpect(status().isOk());
    }

    private String getDomainMirrorJson() throws JsonProcessingException {
        DomainMirror domainMirror = new DomainModel(Map.of(), "test.package");

        ObjectMapper mapper = new ObjectMapper();
        DomainSerializer serializer = new JacksonDomainSerializer(false);
        String jsonObject = serializer.serialize(domainMirror);
        jsonObject = "{\"domainMirror\":" + jsonObject + "}";
        return jsonObject;
    }

    private String getDomainMirrorAndDomainCallsJson() {
        DomainMirror domainMirror = new DomainModel(Map.of(), "test.package");
        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        String domainMirrorJson = domainSerializer.serialize(domainMirror);

        DomainCalls domainCalls = DomainCalls.builder().build();
        DomainCallsSerializer domainCallsSerializer = new JacksonDomainCallsSerializer(false);
        String domainCallsJson = domainCallsSerializer.serialize(domainCalls);

        return "{\"domainMirror\":" + domainMirrorJson
            + ",\"domainCalls\":" + domainCallsJson
            + ",\"domainModelPackages\":[\"test.package\"]}";
    }

    private static byte[] gzip(String value) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(value.getBytes(StandardCharsets.UTF_8));
        }
        return byteStream.toByteArray();
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
