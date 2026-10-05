package io.domainlifecycles.diagramviewer.perf;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.service.DiagramRendering;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectModel;
import io.domainlifecycles.diagramviewer.service.ProjectModelCache;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramFilterComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramFlowFilterComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramVisibilityComponent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.staticanalysis.DomainCallFlowAnalyzer;
import io.domainlifecycles.staticanalysis.FlowConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance measurement for large projects: how long it takes to upload and open a large project and to render
 * its diagrams - generating the nomnoml text, converting it via Kroki, building the flow analyzer, rendering in the
 * background - and to build the filter components of the user interface.
 * <p>
 * Not part of the regular test runs: it needs a captured upload request body of a large project, i.e. the gzip
 * compressed body the DLC build plugin sends to {@code PUT /api/upload/domain-mirror/<project>} (captured e.g. by
 * pointing the plugin's {@code diagramViewerBaseUrl} to a small HTTP server that stores the request body). Pass it as
 * {@code -Dperf.upload=<gzip file>} and run {@code ./gradlew perfTest -Dperf.upload=...}, optionally with
 * {@code -PperfHeap=<heap>} (default 2g). Results are printed and written to {@code build/perf/}.
 * The scenario constants target the esprit_2 model (whole {@code de.mercator.esprit}); override them via
 * {@code -Dperf.package=...} and {@code -Dperf.flowEntry=...} for another model.
 */
@SpringBootTest(properties = "regenerateDiagramsTask.rate=3600000")
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "perf.upload", matches = ".+")
class LargeProjectRendering_PerfTest extends BaseIntegrationTest {

    private static final String PACKAGE = System.getProperty("perf.package", "de.mercator.esprit.vertrag");
    private static final String FLOW_ENTRY = System.getProperty("perf.flowEntry",
        "de.mercator.esprit.vertrag.core.application.AfaEintragDriver");

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
    ProjectModelCache projectModelCache;

    @Autowired
    KrokiClient krokiClient;

    private final Map<String, String> results = new LinkedHashMap<>();
    private AppUser appUser;
    private Project project;

    @BeforeEach
    void setUp() {
        appUser = appUserRepository.save(AppUser.builder()
            .firstName("Perf").lastName("Tester").emailAddress("perf-tester@gmail.com")
            .status(UserStatus.ACTIVE).build());
        project = projectRepository.save(Project.builder()
            .name("perf_project").diagrams(new HashSet<>())
            .assignedUsers(new HashSet<>(Set.of(appUser))).creator(appUser).build());
        UI.setCurrent(new UI());
    }

    @AfterEach
    void tearDown() throws IOException {
        UI.setCurrent(null);
        sessionStorage.delete(project.getId());
        regenerateDiagramsJobRepository.deleteAll();
        diagramRepository.deleteAll();
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
        writeResults();
    }

    @Test
    void measureLargeProjectRendering() throws Exception {
        byte[] upload = Files.readAllBytes(Path.of(System.getProperty("perf.upload")));
        record("upload.bodyGzipMB", String.format("%.1f", upload.length / 1048576.0));

        timed("upload.ms", () -> mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", appUser.getApiKey().toString())
                .header("Content-Encoding", "gzip")
                .content(upload))
            .andExpect(status().isOk()));

        ProjectModel model = timed("open.projectModel.ms", () -> projectModelCache.get(project.getId()));
        record("open.types", String.valueOf(model.domainMirror().getAllDomainTypeMirrors().size()));
        var domainCalls = timed("flow.loadDomainCalls.ms", () -> model.domainCalls().orElseThrow());
        for (int run = 1; run <= 3; run++) {
            timed("flow.buildFlowAnalyzer.run" + run + ".ms",
                () -> new DomainCallFlowAnalyzer(model.domainMirror(), domainCalls, FlowConfig.defaults()));
        }

        Map<String, DomainModelVisibility> scenarios = new LinkedHashMap<>();
        scenarios.put("full", new DomainModelVisibility());
        scenarios.put("package", new DomainModelVisibility().replaceExplicitlyIncludedPackagesNames(Set.of(PACKAGE)));
        scenarios.put("flowForward", new DomainModelVisibility().replaceIncludeFlowsFrom(Set.of(FLOW_ENTRY)));
        scenarios.put("flowBackward", new DomainModelVisibility().replaceIncludeFlowsTo(Set.of(FLOW_ENTRY)));

        for (var scenario : scenarios.entrySet()) {
            measureScenario(scenario.getKey(), scenario.getValue(), model);
        }
        measureUiComponents();
    }

    private void measureScenario(String name, DomainModelVisibility visibility, ProjectModel model) {
        String prefix = "scenario." + name + ".";
        var calls = visibility.hasFlowSettings() ? model.domainCalls().orElse(null) : null;
        String nomnoml = null;
        for (int run = 1; run <= 2; run++) {
            nomnoml = timed(prefix + "nomnoml.run" + run + ".ms", () -> DiagrammerUtils.generateNomnoml(
                model.domainMirror(), DiagramStylingConfiguration.builder().build(), visibility, List.of(), calls));
        }
        record(prefix + "classes", String.valueOf(DiagrammerUtils.countClasses(nomnoml)));
        record(prefix + "nomnomlKB", String.valueOf(nomnoml.length() / 1024));
        final String text = nomnoml;
        try {
            byte[] svg = timed(prefix + "kroki.ms", () -> krokiClient.convert(text));
            record(prefix + "svgKB", String.valueOf(svg.length / 1024));
        } catch (RuntimeException e) {
            record(prefix + "kroki.error", e.getMessage().replaceAll("\\s+", " "));
        }
        Diagram created = null;
        try {
            created = timed(prefix + "createDiagram.ms",
                () -> diagramService.create(project, "perf-" + name, visibility, new DiagramStylingConfiguration()));
        } catch (RuntimeException e) {
            record(prefix + "createDiagram.error", e.getMessage().replaceAll("\\s+", " "));
        }
        if (created != null) {
            measureBackgroundRendering(prefix, created);
        }
    }

    /** What the user interface waits for since 3.2 (saving the model), and when the image is there. */
    private void measureBackgroundRendering(String prefix, Diagram diagram) {
        long start = System.nanoTime();
        DiagramRendering rendering = diagramService.updateModelAndImageAsync(diagram);
        record(prefix + "async.requestReturns.ms", String.valueOf((System.nanoTime() - start) / 1_000_000));
        try {
            DiagramRendering.Result result = rendering.image().get(5, TimeUnit.MINUTES);
            record(prefix + "async.imageSaved.ms", String.valueOf((System.nanoTime() - start) / 1_000_000));
            record(prefix + "async.large", String.valueOf(result.large()));
        } catch (Exception e) {
            record(prefix + "async.error", String.valueOf(e.getMessage()).replaceAll("\\s+", " "));
        }
    }

    private void measureUiComponents() {
        Diagram diagram = diagramRepository.findAll().iterator().next();
        measureComponent("ui.filterComponent", () -> {
            var component = new DiagramFilterComponent(sessionStorage, diagramService);
            component.setDiagram(diagram);
            return component;
        });
        measureComponent("ui.flowFilterComponent", () -> {
            var component = new DiagramFlowFilterComponent(sessionStorage, diagramService);
            component.setDiagram(diagram);
            return component;
        });
        measureComponent("ui.visibilityComponent.collapsed", () -> {
            var component = new DiagramVisibilityComponent(sessionStorage, diagramService);
            component.setDiagram(diagram);
            return component;
        });
        for (DomainType type : DomainType.values()) {
            sessionStorage.setDomainTypeSettingOpen(type, true);
        }
        measureComponent("ui.visibilityComponent.allGroupsOpen", () -> {
            var component = new DiagramVisibilityComponent(sessionStorage, diagramService);
            component.setDiagram(diagram);
            return component;
        });
    }

    private void measureComponent(String key, Callable<Component> build) {
        Component component = timed(key + ".ms", build);
        record(key + ".components", String.valueOf(descendants(component).count()));
    }

    private static Stream<Component> descendants(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(LargeProjectRendering_PerfTest::descendants));
    }

    private <T> T timed(String key, Callable<T> action) {
        long start = System.nanoTime();
        try {
            T result = action.call();
            record(key, String.valueOf((System.nanoTime() - start) / 1_000_000));
            return result;
        } catch (RuntimeException e) {
            record(key, "FAILED after " + (System.nanoTime() - start) / 1_000_000 + " ms");
            throw e;
        } catch (Exception e) {
            record(key, "FAILED after " + (System.nanoTime() - start) / 1_000_000 + " ms");
            throw new IllegalStateException(e);
        }
    }

    private void record(String key, String value) {
        results.put(key, value);
        System.out.println("PERF | " + key + " | " + value);
    }

    private void writeResults() throws IOException {
        Path dir = Path.of("build", "perf");
        Files.createDirectories(dir);
        List<String> lines = new ArrayList<>();
        lines.add("# LargeProjectRendering_PerfTest " + LocalDateTime.now() + " upload=" + System.getProperty("perf.upload"));
        results.forEach((k, v) -> lines.add(k + " = " + v));
        try {
            Files.write(dir.resolve("large-project-rendering-" + System.currentTimeMillis() + ".txt"), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
