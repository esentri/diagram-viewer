package io.domainlifecycles.diagramviewer.scenario;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegenerateDiagramsJobRepository;
import io.domainlifecycles.diagramviewer.service.BoundedContext;
import io.domainlifecycles.diagramviewer.service.BoundedContextAnalysisService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramFilterComponent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    @Autowired
    DiagramService diagramService;

    @Autowired
    ProjectService projectService;

    @Autowired
    BoundedContextAnalysisService boundedContextAnalysisService;

    @Value("${diagrams.location}")
    private String diagramsLocation;

    private AppUser appUser;
    private Project project;

    @BeforeEach
    void setUp() {
        UI.setCurrent(new UI());
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
        UI.setCurrent(null);
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

    @Test
    void Should_NotOfferBoundedContexts_When_DlcFallbackHasOneBoundedContextPerDomainModelPackage() throws Exception {

        // when: two domain model packages, no annotations - two nameless Bounded Contexts, one per package
        upload(RezeptionScenario.gzippedUploadRequestBodyWithTwoDomainModelPackages());

        // then: the upload keeps the packages at the project, which tells the fallback apart
        assertThat(projectRepository.findDomainModelPackages(project.getId()))
            .containsExactlyInAnyOrder(RezeptionScenario.BUCHUNG_CONTEXT_PACKAGE, RezeptionScenario.ZIMMER_CONTEXT_PACKAGE);
        assertThat(sessionStorage.getBoundedContexts(project.getId())).hasSize(2);
        assertThat(sessionStorage.hasDeclaredBoundedContexts(project.getId())).isFalse();
    }

    @Test
    void Should_RenderOnlyTheIncludedBoundedContext() throws Exception {

        // given
        upload(RezeptionScenario.gzippedUploadRequestBodyWithBoundedContexts());
        DomainModelVisibility onlyZimmer = new DomainModelVisibility()
            .replaceIncludedBoundedContextPackages(Set.of(RezeptionScenario.ZIMMER_CONTEXT_PACKAGE));

        // when
        String nomnoml = DiagrammerUtils.generateNomnoml(sessionStorage.getDomainMirror(project.getId()),
            DiagramStylingConfiguration.builder().build(), onlyZimmer, List.of(), null);

        // then
        assertThat(nomnoml).contains("[<AR> Zimmer");
        assertThat(nomnoml).doesNotContain("[<AR> Buchung");
    }

    @Test
    void Should_OfferBoundedContextFilter_And_PersistTheSelection() throws Exception {

        // given
        upload(RezeptionScenario.gzippedUploadRequestBodyWithBoundedContexts());
        Diagram diagram = diagramService.create(project, "bounded-context-filter", new DomainModelVisibility(),
            new DiagramStylingConfiguration());
        DiagramFilterComponent filterComponent = new DiagramFilterComponent(sessionStorage, diagramService);
        filterComponent.setDiagram(diagram);
        MultiSelectComboBox<BoundedContext> boundedContextFilter = boundedContextFilter(filterComponent).orElseThrow();
        assertThat(boundedContextFilter.getListDataView().getItems().map(BoundedContext::label))
            .containsExactly(RezeptionScenario.BUCHUNG_CONTEXT_NAME, RezeptionScenario.AUSLASTUNG_CONTEXT_PACKAGE,
                RezeptionScenario.ZIMMER_CONTEXT_NAME);

        // when
        boundedContextFilter.setValue(boundedContextFilter.getListDataView().getItems()
            .filter(boundedContext -> boundedContext.label().equals(RezeptionScenario.BUCHUNG_CONTEXT_NAME))
            .collect(Collectors.toSet()));

        // then
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        assertThat(reloaded.getDomainModelVisibility().getIncludedBoundedContextPackages())
            .containsExactly(RezeptionScenario.BUCHUNG_CONTEXT_PACKAGE);
    }

    @Test
    void Should_NotOfferBoundedContextFilter_When_DomainModelOnlyHasDlcFallback() throws Exception {

        // given
        upload(RezeptionScenario.gzippedUploadRequestBody());
        Diagram diagram = diagramService.create(project, "no-bounded-context-filter", new DomainModelVisibility(),
            new DiagramStylingConfiguration());
        DiagramFilterComponent filterComponent = new DiagramFilterComponent(sessionStorage, diagramService);

        // when
        filterComponent.setDiagram(diagram);

        // then
        assertThat(boundedContextFilter(filterComponent)).isEmpty();
    }

    @Test
    void Should_CreateDirectoryAndDiagramsPerBoundedContext_When_ProjectIsAnalyzed() throws Exception {

        // given
        upload(RezeptionScenario.gzippedUploadRequestBodyWithBoundedContexts());

        // when
        BoundedContextAnalysisService.Result result = boundedContextAnalysisService.analyze(reloadedProject());

        // then: all images rendered in the background
        CompletableFuture.allOf(result.renderings().toArray(CompletableFuture[]::new)).get(2, TimeUnit.MINUTES);
        assertThat(result.renderings()).allSatisfy(rendering -> assertThat(rendering.get().saved()).isTrue());
        assertThat(result.boundedContexts()).isEqualTo(3);
        assertThat(result.createdDiagrams()).isEqualTo(6);
        assertThat(result.flowsSkipped()).isFalse();

        Project analyzed = reloadedProject();
        assertThat(analyzed.getTopLevelDiagramDirectories()).extracting(DiagramDirectory::getName)
            .containsExactlyInAnyOrder(RezeptionScenario.BUCHUNG_CONTEXT_NAME, RezeptionScenario.ZIMMER_CONTEXT_NAME,
                RezeptionScenario.AUSLASTUNG_CONTEXT_PACKAGE);

        // Buchung: its aggregates, and its three commands - but no read models, so no such folder
        DiagramDirectory buchung = directory(analyzed, null, RezeptionScenario.BUCHUNG_CONTEXT_NAME);
        assertThat(diagramNames(buchung)).containsExactly("Buchung - Aggregates");
        assertThat(analyzed.getSubDirectories(buchung)).extracting(DiagramDirectory::getName)
            .containsExactly(BoundedContextAnalysisService.COMMANDS_DIRECTORY);
        DiagramDirectory commands = directory(analyzed, buchung, BoundedContextAnalysisService.COMMANDS_DIRECTORY);
        assertThat(diagramNames(commands)).containsExactlyInAnyOrder(
            "Buchung - AktualisiereGastdaten", "Buchung - CheckeGastAus", "Buchung - CheckeGastEin");

        // the check-out command: the flow it triggers, and what leads into the method processing it
        Diagram checkOut = diagram(commands, "Buchung - CheckeGastAus");
        assertThat(checkOut.getDomainModelVisibility().getIncludeFlowsFrom())
            .containsExactly(RezeptionScenario.CHECK_OUT_COMMAND);
        assertThat(checkOut.getDomainModelVisibility().getIncludeFlowsTo())
            .containsExactly(RezeptionScenario.CHECK_OUT_METHOD_FLOW_STARTING_POINT);

        // the aggregates diagram is restricted to its Bounded Context and shows aggregates only
        Diagram buchungAggregates = diagram(buchung, "Buchung - Aggregates");
        assertThat(buchungAggregates.getDomainModelVisibility().getIncludedBoundedContextPackages())
            .containsExactly(RezeptionScenario.BUCHUNG_CONTEXT_PACKAGE);
        assertThat(buchungAggregates.getDiagramStylingConfiguration().isShowAggregates()).isTrue();
        assertThat(buchungAggregates.getDiagramStylingConfiguration().isShowApplicationServices()).isFalse();
        String svg = Files.readString(Path.of(diagramsLocation, project.getId().toString(), "Buchung - Aggregates.svg"));
        assertThat(svg).contains("data-name=\"Buchung &lt;&lt;").doesNotContain("data-name=\"Zimmer &lt;&lt;");

        // Zimmer: its aggregates only
        DiagramDirectory zimmer = directory(analyzed, null, RezeptionScenario.ZIMMER_CONTEXT_NAME);
        assertThat(diagramNames(zimmer)).containsExactly("Zimmer - Aggregates");
        assertThat(analyzed.getSubDirectories(zimmer)).isEmpty();

        // the nameless context, labelled by its package: no aggregates, one read model with a backward flow
        DiagramDirectory auslastung = directory(analyzed, null, RezeptionScenario.AUSLASTUNG_CONTEXT_PACKAGE);
        assertThat(diagramNames(auslastung)).isEmpty();
        DiagramDirectory readModels = directory(analyzed, auslastung, BoundedContextAnalysisService.READ_MODELS_DIRECTORY);
        Diagram zimmerauslastung = diagram(readModels, RezeptionScenario.AUSLASTUNG_CONTEXT_PACKAGE + " - Zimmerauslastung");
        assertThat(zimmerauslastung.getDomainModelVisibility().getIncludeFlowsTo())
            .containsExactly(RezeptionScenario.ZIMMERAUSLASTUNG_READ_MODEL);
    }

    @Test
    void Should_ReportProgress_When_ProjectIsAnalyzedInTheBackground() throws Exception {

        // given
        upload(RezeptionScenario.gzippedUploadRequestBodyWithBoundedContexts());
        List<String> progress = new CopyOnWriteArrayList<>();

        // when
        BoundedContextAnalysisService.Result result = boundedContextAnalysisService
            .analyzeAsync(reloadedProject(), (done, total, diagramName) -> progress.add(done + "/" + total + " " + diagramName))
            .get(2, TimeUnit.MINUTES);
        awaitRenderings(result);

        // then: every diagram reported once, counting up to the total
        assertThat(result.createdDiagrams()).isEqualTo(6);
        assertThat(progress).hasSize(6);
        assertThat(progress.get(0)).startsWith("1/6 ");
        assertThat(progress.get(5)).startsWith("6/6 ");
        assertThat(progress).anyMatch(entry -> entry.endsWith(" Buchung - CheckeGastAus"));
    }

    @Test
    void Should_OnlyAddWhatIsMissing_When_ProjectIsAnalyzedAgain() throws Exception {

        // given
        upload(RezeptionScenario.gzippedUploadRequestBodyWithBoundedContexts());
        awaitRenderings(boundedContextAnalysisService.analyze(reloadedProject()));

        // when
        BoundedContextAnalysisService.Result second = boundedContextAnalysisService.analyze(reloadedProject());

        // then
        assertThat(second.createdDiagrams()).isZero();
        assertThat(second.skippedDiagrams()).isEqualTo(6);
        Project analyzed = reloadedProject();
        assertThat(analyzed.getDiagrams()).hasSize(6);
        assertThat(analyzed.getDiagramDirectories()).hasSize(5);
    }

    @Test
    void Should_OnlyCreateAggregateDiagrams_When_NoStaticAnalysisResultWasUploaded() throws Exception {

        // given
        upload(RezeptionScenario.gzippedUploadRequestBodyWithoutDomainCalls());

        // when: DLC's fallback - the whole domain model package as one Bounded Context
        BoundedContextAnalysisService.Result result = boundedContextAnalysisService.analyze(reloadedProject());
        awaitRenderings(result);

        // then
        assertThat(result.flowsSkipped()).isTrue();
        Project analyzed = reloadedProject();
        DiagramDirectory rezeption = directory(analyzed, null, RezeptionScenario.DOMAIN_MODEL_PACKAGE);
        assertThat(diagramNames(rezeption)).containsExactly(RezeptionScenario.DOMAIN_MODEL_PACKAGE + " - Aggregates");
        assertThat(analyzed.getSubDirectories(rezeption)).isEmpty();
    }

    private static void awaitRenderings(BoundedContextAnalysisService.Result result) throws Exception {
        CompletableFuture.allOf(result.renderings().toArray(CompletableFuture[]::new)).get(2, TimeUnit.MINUTES);
    }

    private Project reloadedProject() {
        return projectService.getByName(PROJECT_NAME);
    }

    private static DiagramDirectory directory(Project project, DiagramDirectory parent, String name) {
        return project.getSubDirectories(parent).stream()
            .filter(directory -> directory.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No directory '" + name + "'"));
    }

    private static List<String> diagramNames(DiagramDirectory directory) {
        return directory.getDiagrams().stream().map(Diagram::getName).toList();
    }

    private static Diagram diagram(DiagramDirectory directory, String name) {
        return directory.getDiagrams().stream()
            .filter(diagram -> diagram.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No diagram '" + name + "'"));
    }

    @SuppressWarnings("unchecked")
    private static Optional<MultiSelectComboBox<BoundedContext>> boundedContextFilter(Component root) {
        return descendants(root)
            .filter(component -> component.getId().filter("bounded-context-filter"::equals).isPresent())
            .map(component -> (MultiSelectComboBox<BoundedContext>) component)
            .findFirst();
    }

    private static Stream<Component> descendants(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(RezeptionBoundedContexts_ITest::descendants));
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
