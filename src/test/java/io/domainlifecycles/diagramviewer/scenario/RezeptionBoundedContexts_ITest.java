package io.domainlifecycles.diagramviewer.scenario;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegenerateDiagramsJobRepository;
import io.domainlifecycles.diagramviewer.service.BoundedContext;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers projects whose domain model declares Bounded Contexts, based on the rezeption domain with three Bounded
 * Contexts - two of them named - see {@link RezeptionScenario#gzippedUploadRequestBodyWithBoundedContexts()}.
 */
@SpringBootTest(properties = "regenerateDiagramsTask.rate=3600000")
@AutoConfigureMockMvc
class RezeptionBoundedContexts_ITest extends BaseIntegrationTest {

    private static final String PROJECT_NAME = "rezeption_scenario_bounded_contexts";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    DiagramRepository diagramRepository;

    @Autowired
    RegenerateDiagramsJobRepository regenerateDiagramsJobRepository;

    @Autowired
    SessionStorage sessionStorage;

    private AppUser appUser;
    private Project project;

    @BeforeEach
    void setUp() {
        appUser = appUserRepository.save(AppUser.builder()
            .firstName("Rezeption")
            .lastName("BoundedContextTester")
            .emailAddress("rezeption-bounded-context-tester@gmail.com")
            .status(UserStatus.ACTIVE)
            .build());
        project = projectRepository.save(Project.builder()
            .name(PROJECT_NAME)
            .diagrams(new HashSet<>())
            .diagramDirectories(new HashSet<>())
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .creator(appUser)
            .build());
    }

    @AfterEach
    void tearDown() {
        sessionStorage.delete(project.getId());
        regenerateDiagramsJobRepository.deleteAll();
        diagramRepository.deleteAll();
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void Should_OfferDeclaredBoundedContexts_LabelledByNameOrPackage() throws Exception {

        // when
        upload(RezeptionScenario.gzippedUploadRequestBodyWithBoundedContexts());

        // then
        assertThat(sessionStorage.hasDeclaredBoundedContexts(project.getId())).isTrue();
        assertThat(sessionStorage.getBoundedContexts(project.getId()))
            .extracting(BoundedContext::packageName, BoundedContext::label)
            .containsExactly(
                // sorted by label, ignoring case
                tuple(RezeptionScenario.BUCHUNG_CONTEXT_PACKAGE, RezeptionScenario.BUCHUNG_CONTEXT_NAME),
                tuple(RezeptionScenario.AUSLASTUNG_CONTEXT_PACKAGE, RezeptionScenario.AUSLASTUNG_CONTEXT_PACKAGE),
                tuple(RezeptionScenario.ZIMMER_CONTEXT_PACKAGE, RezeptionScenario.ZIMMER_CONTEXT_NAME));
    }

    @Test
    void Should_NotOfferBoundedContexts_When_DomainModelOnlyHasDlcFallback() throws Exception {

        // when: the real upload, whose only Bounded Context is the whole domain model package without a name
        upload(RezeptionScenario.gzippedUploadRequestBody());

        // then
        assertThat(sessionStorage.getBoundedContexts(project.getId()))
            .extracting(BoundedContext::packageName)
            .containsExactly(RezeptionScenario.DOMAIN_MODEL_PACKAGE);
        assertThat(sessionStorage.hasDeclaredBoundedContexts(project.getId())).isFalse();
    }

    private void upload(byte[] gzippedBody) throws Exception {
        mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", appUser.getApiKey().toString())
                .header("Content-Encoding", "gzip")
                .content(gzippedBody))
            .andExpect(status().isOk());
    }
}
