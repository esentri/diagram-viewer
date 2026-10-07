package io.domainlifecycles.diagramviewer.scenario;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.staticanalysis.DomainCalls;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Backend integration test covering the real "rezeption" hotel booking scenario (see
 * {@link RezeptionScenario}) end to end against a real Postgres and Kroki (via Testcontainers):
 * uploading the domain mirror and its static analysis result (DomainCalls) the way the DLC build
 * plugin does, persisting them, keeping DomainCalls deserialized in the session, and restricting a
 * generated diagram to the guest check-out flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RezeptionUploadAndFlowFilter_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_MAIL_ADDRESS = "rezeption-tester@gmail.com";
    private static final String TEST_USER_FIRST_NAME = "Rezeption";
    private static final String TEST_USER_LAST_NAME = "Tester";
    private static final String PROJECT_NAME = "rezeption_scenario";

    @Value("${diagrams.location}")
    private String diagramsLocation;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectDomainMirrorRepository projectDomainMirrorRepository;

    @Autowired
    DiagramRepository diagramRepository;

    @Autowired
    DiagramService diagramService;

    @Autowired
    ProjectDomainMirrorService projectDomainMirrorService;

    @Autowired
    SessionStorage sessionStorage;

    private AppUser appUser;
    private Project project;

    @BeforeEach
    void setUp() throws Exception {
        appUser = appUserRepository.save(AppUser.builder()
            .firstName(TEST_USER_FIRST_NAME)
            .lastName(TEST_USER_LAST_NAME)
            .emailAddress(TEST_USER_MAIL_ADDRESS)
            .status(UserStatus.ACTIVE)
            .build());

        project = projectRepository.save(Project.builder()
            .name(PROJECT_NAME)
            .diagrams(new HashSet<>())
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .creator(appUser)
            .build());

        // upload the real rezeption DomainMirror + DomainCalls, gzip-compressed, exactly as the
        // DLC build plugin (both its plain and its streaming upload) sends it
        mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", appUser.getApiKey().toString())
                .header("Content-Encoding", "gzip")
                .content(RezeptionScenario.gzippedUploadRequestBody()))
            .andExpect(status().isOk());
    }

    @AfterEach
    void tearDown() {
        sessionStorage.delete(project.getId());
        diagramRepository.deleteAll();
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void Should_PersistRealRezeptionDomainMirrorAndDomainCalls() {

        // when
        ProjectDomainMirror stored = projectDomainMirrorRepository.findByProjectId(project.getId()).orElseThrow();
        DomainMirror domainMirror = projectDomainMirrorService.getDomainMirror(project.getId());

        // then: stored compressed only, and readable back
        assertThat(stored.getDomainMirrorGz()).isNotEmpty();
        assertThat(stored.getDomainCallsGz()).isNotEmpty();
        assertThat(stored.getDomainCalls()).isNull();
        assertThat(domainMirror.getDomainTypeMirror(RezeptionScenario.BUCHUNG_AGGREGATE)).isPresent();
        assertThat(domainMirror.getDomainTypeMirror(RezeptionScenario.ZIMMER_AGGREGATE)).isPresent();
        assertThat(projectDomainMirrorService.loadDomainCalls(project.getId(), domainMirror)).isPresent();
    }

    @Test
    void Should_ExposeDeserializedDomainCallsInSession_When_ProjectIsOpened() {

        // when
        Optional<DomainCalls> domainCalls = sessionStorage.getDomainCalls(project.getId());

        // then
        assertThat(domainCalls).isPresent();
        assertThat(domainCalls.get().callers()).isNotEmpty();
    }

    @Test
    void Should_RestrictDiagramToCheckOutFlow_When_FlowFilterConfiguredForTheCheckOutCommand() throws IOException {

        // given
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeFlowsFrom(Set.of(RezeptionScenario.CHECK_OUT_COMMAND));

        // when
        Diagram diagram = diagramService.create(project, "check-out-flow-command", visibility, new DiagramStylingConfiguration());

        // then: the flow-relevant types are rendered as their own class boxes; an unrelated command
        // is not - even though its type name still appears in a *rendered method's* signature, since
        // the flow filter restricts which types are included, not which members an included type shows
        String svg = readGeneratedSvg(diagram);
        assertThat(svg).contains(classBoxMarker("Buchung"));
        assertThat(svg).contains(classBoxMarker("Zimmer"));
        assertThat(svg).doesNotContain(classBoxMarker("AktualisiereGastdaten"));
        assertThat(svg).doesNotContain(classBoxMarker("CheckeGastEin"));
    }

    @Test
    void Should_RestrictDiagramToCheckOutFlow_When_FlowFilterConfiguredForTheApplicationServiceMethod() throws IOException {

        // given
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeFlowsFrom(Set.of(RezeptionScenario.CHECK_OUT_METHOD_FLOW_STARTING_POINT));

        // when
        Diagram diagram = diagramService.create(project, "check-out-flow-method", visibility, new DiagramStylingConfiguration());

        // then
        String svg = readGeneratedSvg(diagram);
        assertThat(svg).contains(classBoxMarker("Buchung"));
        assertThat(svg).doesNotContain(classBoxMarker("AktualisiereGastdaten"));
    }

    @Test
    void Should_RestrictDiagramToTheEntryChannels_When_BackwardFlowFilterConfiguredForTheCheckOutEvent() throws IOException {

        // given
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeFlowsTo(Set.of(RezeptionScenario.GAST_AUSGECHECKT_EVENT));

        // when
        Diagram diagram = diagramService.create(project, "check-out-backward-flow-event", visibility, new DiagramStylingConfiguration());

        // then: what leads into the event is rendered, the unrelated check-in command and the
        // event's own (downstream) listener are not
        String svg = readGeneratedSvg(diagram);
        assertThat(svg).contains(classBoxMarker("Buchung"));
        assertThat(svg).contains(classBoxMarker("CheckeGastAus"));
        assertThat(svg).doesNotContain(classBoxMarker("CheckeGastEin"));
        assertThat(svg).doesNotContain(classBoxMarker("ZimmerFreigabeListener"));
        assertThat(diagramRepository.findById(diagram.getId()).orElseThrow()
            .getDomainModelVisibility().getIncludeFlowsTo())
            .containsExactly(RezeptionScenario.GAST_AUSGECHECKT_EVENT);
    }

    private String readGeneratedSvg(Diagram diagram) throws IOException {
        Path svgPath = Path.of(diagramsLocation, project.getId().toString(), DiagramFileUtils.imageFileName(diagram));
        return Files.readString(svgPath);
    }

    /**
     * The nomnoml/Kroki SVG marker for a type rendered as its own class box (its stereotyped
     * heading), as opposed to merely being named in another rendered type's method signature.
     */
    private static String classBoxMarker(String shortTypeName) {
        return "data-name=\"" + shortTypeName + " &lt;&lt;";
    }
}
