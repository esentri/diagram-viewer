package io.domainlifecycles.diagramviewer.scenario;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import io.domainlifecycles.diagramviewer.service.BoundedContext;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramFilterComponent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    @Autowired
    DiagramService diagramService;

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
