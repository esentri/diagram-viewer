package io.domainlifecycles.diagramviewer.scenario;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramFlowFilterComponent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.api.MethodMirror;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "Frontend workflow" test for the two-stage flow filter (class, then that class' methods),
 * driving the real {@link DiagramFlowFilterComponent} - the same server-side component the browser
 * UI renders - directly, the way Vaadin Flow components can be exercised without a browser: by
 * calling their public API and firing the same events a client interaction would raise.
 * <p>
 * Uses the real "rezeption" hotel booking scenario (see {@link RezeptionScenario}), continuing where
 * {@link RezeptionUploadAndFlowFilter_ITest} leaves off, but from the UI component's perspective:
 * selecting a class, seeing its methods offered, adding a flow, and removing it again.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RezeptionFlowFilterFrontend_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_MAIL_ADDRESS = "rezeption-frontend-tester@gmail.com";
    private static final String TEST_USER_FIRST_NAME = "Rezeption";
    private static final String TEST_USER_LAST_NAME = "FrontendTester";
    private static final String PROJECT_NAME = "rezeption_scenario_frontend";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    DiagramRepository diagramRepository;

    @Autowired
    DiagramService diagramService;

    @Autowired
    SessionStorage sessionStorage;

    private AppUser appUser;
    private Project project;
    private Diagram diagram;
    private DiagramFlowFilterComponent flowFilterComponent;

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

        mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", appUser.getApiKey().toString())
                .header("Content-Encoding", "gzip")
                .content(RezeptionScenario.gzippedUploadRequestBody()))
            .andExpect(status().isOk());

        diagram = diagramService.create(project, "flow-filter-ui", new DomainModelVisibility(), new DiagramStylingConfiguration());

        UI.setCurrent(new UI());
        flowFilterComponent = new DiagramFlowFilterComponent(sessionStorage, diagramService);
        UI.getCurrent().add(flowFilterComponent);
        flowFilterComponent.setDiagram(diagram);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        sessionStorage.delete(project.getId());
        diagramRepository.deleteAll();
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void Should_DisableMethodPicker_And_AddWholeTypeFlow_When_UserSelectsTheCheckOutCommand() {

        // given: the user opens the class picker and selects the check-out command
        ComboBox<DomainTypeMirror> classComboBox = classComboBox(flowFilterComponent);
        DomainTypeMirror checkOutCommand = findByTypeName(classComboBox, RezeptionScenario.CHECK_OUT_COMMAND);

        // when
        classComboBox.setValue(checkOutCommand);

        // then: a command starts the flow it triggers - no method to pick
        assertThat(methodComboBox(flowFilterComponent).isEnabled()).isFalse();

        // when: the user clicks "Add flow"
        click(addFlowButton(flowFilterComponent));

        // then: the flow is active and persisted
        assertThat(activeFlows(flowFilterComponent)).contains(RezeptionScenario.CHECK_OUT_COMMAND);
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsFrom())
            .containsExactly(RezeptionScenario.CHECK_OUT_COMMAND);
    }

    @Test
    void Should_OfferItsMethods_And_AddMethodRestrictedFlow_When_UserSelectsAServiceClassThenOneOfItsMethods() {

        // given: the user selects the application service handling the check-out use case
        ComboBox<DomainTypeMirror> classComboBox = classComboBox(flowFilterComponent);
        DomainTypeMirror applicationService = findByTypeName(classComboBox, RezeptionScenario.BUCHUNG_APPLICATION_SERVICE);

        // when
        classComboBox.setValue(applicationService);

        // then: its methods become selectable
        ComboBox<MethodMirror> methodComboBox = methodComboBox(flowFilterComponent);
        assertThat(methodComboBox.isEnabled()).isTrue();
        MethodMirror checkOutMethod = methodComboBox.getListDataView().getItems()
            .filter(method -> RezeptionScenario.CHECK_OUT_METHOD_NAME.equals(method.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                "Expected method '" + RezeptionScenario.CHECK_OUT_METHOD_NAME + "' to be offered"));

        // when: the user picks that method and clicks "Add flow"
        methodComboBox.setValue(checkOutMethod);
        click(addFlowButton(flowFilterComponent));

        // then: the method-restricted flow is active and persisted
        assertThat(activeFlows(flowFilterComponent)).contains(RezeptionScenario.CHECK_OUT_METHOD_FLOW_STARTING_POINT);
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsFrom())
            .containsExactly(RezeptionScenario.CHECK_OUT_METHOD_FLOW_STARTING_POINT);
    }

    @Test
    void Should_RemoveFlow_When_UserDeselectsItFromTheActiveFlowsList() {

        // given: a flow was added
        ComboBox<DomainTypeMirror> classComboBox = classComboBox(flowFilterComponent);
        classComboBox.setValue(findByTypeName(classComboBox, RezeptionScenario.CHECK_OUT_COMMAND));
        click(addFlowButton(flowFilterComponent));
        assertThat(activeFlows(flowFilterComponent)).contains(RezeptionScenario.CHECK_OUT_COMMAND);

        // when: the user deselects it in the active flows list
        activeFlowsComboBox(flowFilterComponent).orElseThrow().deselect(RezeptionScenario.CHECK_OUT_COMMAND);

        // then: no flow restriction remains, and the active flows list disappears again
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsFrom()).isEmpty();
        assertThat(activeFlowsComboBox(flowFilterComponent)).isEmpty();
    }

    private static DomainTypeMirror findByTypeName(ComboBox<DomainTypeMirror> classComboBox, String typeName) {
        return classComboBox.getListDataView().getItems()
            .filter(type -> typeName.equals(type.getTypeName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected type '" + typeName + "' to be selectable"));
    }

    private static Set<String> activeFlows(Component root) {
        return activeFlowsComboBox(root).map(MultiSelectComboBox::getValue).orElseGet(Set::of);
    }

    @SuppressWarnings("unchecked")
    private static Optional<MultiSelectComboBox<String>> activeFlowsComboBox(Component root) {
        return descendants(root)
            .filter(c -> c instanceof MultiSelectComboBox<?> comboBox
                && comboBox.getLabel() != null && comboBox.getLabel().startsWith("Active flow filters"))
            .map(c -> (MultiSelectComboBox<String>) c)
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<DomainTypeMirror> classComboBox(Component root) {
        return (ComboBox<DomainTypeMirror>) descendants(root)
            .filter(c -> c instanceof ComboBox<?> comboBox && "Class".equals(comboBox.getLabel()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Class combo box not found"));
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<MethodMirror> methodComboBox(Component root) {
        return (ComboBox<MethodMirror>) descendants(root)
            .filter(c -> c instanceof ComboBox<?> comboBox
                && comboBox.getLabel() != null && comboBox.getLabel().startsWith("Method"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Method combo box not found"));
    }

    private static Button addFlowButton(Component root) {
        return (Button) descendants(root)
            .filter(c -> c instanceof Button button && "Add flow".equals(button.getText()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("'Add flow' button not found"));
    }

    private static void click(Button button) {
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    private static Stream<Component> descendants(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(RezeptionFlowFilterFrontend_ITest::descendants));
    }
}
