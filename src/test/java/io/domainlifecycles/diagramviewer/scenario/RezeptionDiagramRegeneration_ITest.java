package io.domainlifecycles.diagramviewer.scenario;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegenerateDiagramsJobRepository;
import io.domainlifecycles.diagramviewer.scheduled.DiagramRegenerationTask;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the regeneration of existing diagrams after their project's domain model was uploaded again.
 * <p>
 * The {@link DiagramRegenerationTask} is a {@code @Scheduled} job, so in production it runs on a
 * scheduler thread without any HTTP request or session bound to it. The test therefore invokes it on a
 * fresh thread: the test thread itself has a mock request bound by the Spring test framework, which
 * would hide any dependency on request or session scoped beans. The scheduled rate is set high enough
 * that the real scheduler does not interfere with the test.
 */
@SpringBootTest(properties = "regenerateDiagramsTask.rate=3600000")
@AutoConfigureMockMvc
class RezeptionDiagramRegeneration_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_MAIL_ADDRESS = "rezeption-regeneration-tester@gmail.com";
    private static final String PROJECT_NAME = "rezeption_scenario_regeneration";

    @Value("${diagrams.location}")
    private String diagramsLocation;

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
    DiagramService diagramService;

    @Autowired
    SessionStorage sessionStorage;

    @Autowired
    DiagramRegenerationTask diagramRegenerationTask;

    private AppUser appUser;
    private Project project;

    @BeforeEach
    void setUp() throws Exception {
        appUser = appUserRepository.save(AppUser.builder()
            .firstName("Rezeption")
            .lastName("RegenerationTester")
            .emailAddress(TEST_USER_MAIL_ADDRESS)
            .status(UserStatus.ACTIVE)
            .build());

        project = projectRepository.save(Project.builder()
            .name(PROJECT_NAME)
            .diagrams(new HashSet<>())
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .creator(appUser)
            .build());

        upload();
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
    void Should_RegenerateExistingDiagram_When_DomainModelIsUploadedAgain() throws Exception {

        // given: a diagram exists, and the domain model is uploaded again, which schedules its regeneration
        Diagram diagram = diagramService.create(project, "regenerated", new DomainModelVisibility(), new DiagramStylingConfiguration());
        upload();
        assertThat(regenerateDiagramsJobRepository.findByDiagramId(diagram.getId())).hasSize(1);

        // when: the regeneration task runs the way the scheduler runs it - on its own thread, without
        // any HTTP request or session bound
        Throwable failure = runOnSchedulerLikeThread(diagramRegenerationTask::regenerateUpdatedDomainMirrors);

        // then: the regeneration succeeded and its job is done
        assertThat(failure).isNull();
        assertThat(regenerateDiagramsJobRepository.findByDiagramId(diagram.getId())).isEmpty();
    }

    @Test
    void Should_AcceptReupload_When_DiagramRegenerationIsStillPending() throws Exception {

        // given: a diagram, and a re-upload whose regeneration job has not run yet (or has failed)
        Diagram diagram = diagramService.create(project, "pending", new DomainModelVisibility(), new DiagramStylingConfiguration());
        upload();
        assertThat(regenerateDiagramsJobRepository.findByDiagramId(diagram.getId())).hasSize(1);

        // when: the project is uploaded once more before the scheduler ran
        upload();

        // then: the upload is accepted and the diagram keeps exactly one pending job
        assertThat(regenerateDiagramsJobRepository.findByDiagramId(diagram.getId())).hasSize(1);
    }

    @Test
    void Should_KeepFlowRestriction_When_FlowFilteredDiagramIsRegenerated() throws Exception {

        // given: a diagram restricted to the check-out flow, and a re-upload scheduling its regeneration
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeFlowsFrom(Set.of(RezeptionScenario.CHECK_OUT_COMMAND));
        Diagram diagram = diagramService.create(project, "regenerated-flow", visibility, new DiagramStylingConfiguration());
        Path svgPath = Path.of(diagramsLocation, project.getId().toString(), diagram.getName() + ".svg");
        Files.delete(svgPath);
        upload();

        // when
        Throwable failure = runOnSchedulerLikeThread(diagramRegenerationTask::regenerateUpdatedDomainMirrors);

        // then: the diagram was rendered again - and still restricted to the flow, which requires the
        // static analysis result to have been handed to the regeneration (without it, flows are ignored)
        assertThat(failure).isNull();
        String svg = Files.readString(svgPath);
        assertThat(svg).contains(classBoxMarker("Buchung"));
        assertThat(svg).doesNotContain(classBoxMarker("CheckeGastEin"));
        assertThat(svg).doesNotContain(classBoxMarker("AktualisiereGastdaten"));
    }

    private static String classBoxMarker(String shortTypeName) {
        return "data-name=\"" + shortTypeName + " &lt;&lt;";
    }

    private void upload() throws Exception {
        mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", appUser.getApiKey().toString())
                .header("Content-Encoding", "gzip")
                .content(RezeptionScenario.gzippedUploadRequestBody()))
            .andExpect(status().isOk());
    }

    private static Throwable runOnSchedulerLikeThread(Runnable task) throws InterruptedException {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                failure.set(t);
            }
        }, "scheduler-like-test-thread");
        thread.start();
        thread.join();
        return failure.get();
    }
}
