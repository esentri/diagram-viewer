package io.domainlifecycles.diagramviewer.scenario;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.RegenerateDiagramsJobRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.FlowTextDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramFlowFilterComponent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.api.MethodMirror;
import java.util.HashSet;
import java.util.List;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    RegenerateDiagramsJobRepository regenerateDiagramsJobRepository;

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
        // a re-upload of the domain model schedules regeneration jobs referencing the diagrams
        regenerateDiagramsJobRepository.deleteAll();
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
    void Should_OfferTheFlowAsText_When_AFlowIsActive() {

        // given: without a flow there is nothing to show
        assertThat(showFlowAsTextButton(flowFilterComponent)).isEmpty();
        ComboBox<DomainTypeMirror> classComboBox = classComboBox(flowFilterComponent);
        classComboBox.setValue(findByTypeName(classComboBox, RezeptionScenario.CHECK_OUT_COMMAND));
        click(addFlowButton(flowFilterComponent));

        // when: the dialog's download link needs a session to register its resource with
        withSession(UI.getCurrent());
        click(showFlowAsTextButton(flowFilterComponent).orElseThrow());

        // then
        FlowTextDialog dialog = descendants(flowFilterComponent)
            .filter(FlowTextDialog.class::isInstance)
            .map(FlowTextDialog.class::cast)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Flow text dialog not opened"));
        assertThat(dialog.isOpened()).isTrue();
        List<String> lines = descendants(dialog)
            .filter(c -> "flow-text".equals(c.getId().orElse(null)))
            .flatMap(Component::getChildren)
            .map(line -> line.getElement().getText())
            .toList();
        assertThat(lines).contains("▼ WHAT IT LEADS TO", "[Command] CheckeGastAus   ◀ start");

        // when
        dialog.close();

        // then
        assertThat(descendants(flowFilterComponent).noneMatch(FlowTextDialog.class::isInstance)).isTrue();
    }

    @Test
    void Should_ShowOnlyTheFlowMethodsByDefault_And_KeepTheChoice_When_UserSwitchesItOff() {

        // given: the switch is shown in the flow filter, checked by default
        Checkbox showOnlyFlowMethods = showOnlyFlowMethodsCheckbox(flowFilterComponent);
        assertThat(showOnlyFlowMethods.getValue()).isTrue();

        // when
        showOnlyFlowMethods.setValue(false);

        // then: the choice is persisted and still shown after refreshing
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        assertThat(reloaded.getDiagramStylingConfiguration().isShowOnlyFlowMethods()).isFalse();
        assertThat(showOnlyFlowMethodsCheckbox(flowFilterComponent).getValue()).isFalse();
    }

    @Test
    void Should_ConnectTheCallsOfTheFlowsByDefault_And_KeepTheChoice_When_UserSwitchesItOff() {

        // given: the switch is shown in the flow filter, checked by default
        Checkbox showFlowCallRelations = checkbox(flowFilterComponent, "show-flow-call-relations");
        assertThat(showFlowCallRelations.getValue()).isTrue();

        // when
        showFlowCallRelations.setValue(false);

        // then: the choice is persisted and still shown after refreshing
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        assertThat(reloaded.getDiagramStylingConfiguration().isShowFlowCallRelations()).isFalse();
        assertThat(checkbox(flowFilterComponent, "show-flow-call-relations").getValue()).isFalse();
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

    @Test
    void Should_HideCommands_And_AddBackwardFlow_When_UserSwitchesToBackwardDirectionAndSelectsTheCheckOutEvent() {

        // given: the user switches the direction to backward
        directionRadioGroup(flowFilterComponent).setValue(DiagramFlowFilterComponent.FlowDirection.BACKWARD);

        // then: a domain command cannot be a backward flow target, so none is offered anymore
        ComboBox<DomainTypeMirror> classComboBox = classComboBox(flowFilterComponent);
        assertThat(classComboBox.getListDataView().getItems()
            .map(DomainTypeMirror::getTypeName))
            .doesNotContain(RezeptionScenario.CHECK_OUT_COMMAND, RezeptionScenario.CHECK_IN_COMMAND);

        // when: the user selects the check-out event and clicks "Add flow"
        classComboBox.setValue(findByTypeName(classComboBox, RezeptionScenario.GAST_AUSGECHECKT_EVENT));
        assertThat(methodComboBox(flowFilterComponent).isEnabled()).isFalse();
        click(addFlowButton(flowFilterComponent));

        // then: the backward flow is active and persisted, the forward flows stay untouched
        assertThat(activeBackwardFlows(flowFilterComponent)).contains(RezeptionScenario.GAST_AUSGECHECKT_EVENT);
        assertThat(activeFlowsComboBox(flowFilterComponent)).isEmpty();
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsTo())
            .containsExactly(RezeptionScenario.GAST_AUSGECHECKT_EVENT);
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsFrom()).isEmpty();
    }

    @Test
    void Should_KeepForwardAndBackwardFlowsApart_When_UserAddsOneOfEach() {

        // given: a forward flow from the check-out command
        ComboBox<DomainTypeMirror> classComboBox = classComboBox(flowFilterComponent);
        classComboBox.setValue(findByTypeName(classComboBox, RezeptionScenario.CHECK_OUT_COMMAND));
        click(addFlowButton(flowFilterComponent));

        // when: a backward flow into the booking application service is added as well
        directionRadioGroup(flowFilterComponent).setValue(DiagramFlowFilterComponent.FlowDirection.BACKWARD);
        classComboBox = classComboBox(flowFilterComponent);
        classComboBox.setValue(findByTypeName(classComboBox, RezeptionScenario.BUCHUNG_APPLICATION_SERVICE));
        click(addFlowButton(flowFilterComponent));

        // then
        assertThat(activeFlows(flowFilterComponent)).containsExactly(RezeptionScenario.CHECK_OUT_COMMAND);
        assertThat(activeBackwardFlows(flowFilterComponent)).containsExactly(RezeptionScenario.BUCHUNG_APPLICATION_SERVICE);

        // when: the backward flow is removed again
        activeBackwardFlowsComboBox(flowFilterComponent).orElseThrow().deselect(RezeptionScenario.BUCHUNG_APPLICATION_SERVICE);

        // then: only the forward flow remains
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsTo()).isEmpty();
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsFrom())
            .containsExactly(RezeptionScenario.CHECK_OUT_COMMAND);
        assertThat(activeBackwardFlowsComboBox(flowFilterComponent)).isEmpty();
    }

    @Test
    void Should_DisableFlowFiltering_And_ShowConfiguredFlowsAsInactive_When_DomainModelIsReuploadedWithoutStaticAnalysis() throws Exception {

        // given: a forward and a backward flow were added while an analysis result was available
        ComboBox<DomainTypeMirror> classComboBox = classComboBox(flowFilterComponent);
        classComboBox.setValue(findByTypeName(classComboBox, RezeptionScenario.CHECK_OUT_COMMAND));
        click(addFlowButton(flowFilterComponent));
        directionRadioGroup(flowFilterComponent).setValue(DiagramFlowFilterComponent.FlowDirection.BACKWARD);
        classComboBox = classComboBox(flowFilterComponent);
        classComboBox.setValue(findByTypeName(classComboBox, RezeptionScenario.GAST_AUSGECHECKT_EVENT));
        click(addFlowButton(flowFilterComponent));

        // when: the domain model is uploaded again, this time without a static analysis result
        mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", appUser.getApiKey().toString())
                .header("Content-Encoding", "gzip")
                .content(RezeptionScenario.gzippedUploadRequestBodyWithoutDomainCalls()))
            .andExpect(status().isOk());
        Diagram reloaded = diagramRepository.findById(diagram.getId()).orElseThrow();
        flowFilterComponent.setDiagram(reloaded);

        // then: no flow can be added anymore ...
        assertThat(descendants(flowFilterComponent)
            .noneMatch(c -> c instanceof ComboBox<?> comboBox && "Class".equals(comboBox.getLabel()))).isTrue();
        assertThat(descendants(flowFilterComponent)
            .noneMatch(c -> c instanceof Button button && "Add flow".equals(button.getText()))).isTrue();

        // ... the configured flows are kept, shown read-only as inactive ...
        MultiSelectComboBox<String> inactiveFlows = inactiveFlowsComboBox(flowFilterComponent).orElseThrow();
        assertThat(inactiveFlows.isReadOnly()).isTrue();
        assertThat(inactiveFlows.getValue())
            .containsExactlyInAnyOrder(RezeptionScenario.CHECK_OUT_COMMAND, RezeptionScenario.GAST_AUSGECHECKT_EVENT);
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsFrom()).containsExactly(RezeptionScenario.CHECK_OUT_COMMAND);
        assertThat(reloaded.getDomainModelVisibility().getIncludeFlowsTo()).containsExactly(RezeptionScenario.GAST_AUSGECHECKT_EVENT);

        // ... and the diagram still renders, just unrestricted
        diagramService.updateModelAndImage(reloaded);
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

    private static Set<String> activeBackwardFlows(Component root) {
        return activeBackwardFlowsComboBox(root).map(MultiSelectComboBox::getValue).orElseGet(Set::of);
    }

    @SuppressWarnings("unchecked")
    private static Optional<MultiSelectComboBox<String>> activeBackwardFlowsComboBox(Component root) {
        return descendants(root)
            .filter(c -> c instanceof MultiSelectComboBox<?> comboBox
                && comboBox.getLabel() != null && comboBox.getLabel().startsWith("Active backward flow filters"))
            .map(c -> (MultiSelectComboBox<String>) c)
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    private static Optional<MultiSelectComboBox<String>> inactiveFlowsComboBox(Component root) {
        return descendants(root)
            .filter(c -> c instanceof MultiSelectComboBox<?> comboBox
                && comboBox.getLabel() != null && comboBox.getLabel().startsWith("Inactive flow filters"))
            .map(c -> (MultiSelectComboBox<String>) c)
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    private static RadioButtonGroup<DiagramFlowFilterComponent.FlowDirection> directionRadioGroup(Component root) {
        return (RadioButtonGroup<DiagramFlowFilterComponent.FlowDirection>) descendants(root)
            .filter(c -> c instanceof RadioButtonGroup<?> group && "Direction".equals(group.getLabel()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Direction radio group not found"));
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

    private static Checkbox showOnlyFlowMethodsCheckbox(Component root) {
        return checkbox(root, "show-only-flow-methods");
    }

    private static Checkbox checkbox(Component root, String id) {
        return (Checkbox) descendants(root)
            .filter(c -> c instanceof Checkbox && id.equals(c.getId().orElse(null)))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Checkbox '" + id + "' not found"));
    }

    private static Optional<Button> showFlowAsTextButton(Component root) {
        return descendants(root)
            .filter(c -> c instanceof Button button && "Show flow as text".equals(button.getText()))
            .map(Button.class::cast)
            .findFirst();
    }

    private static void withSession(UI ui) {
        VaadinSession session = mock(VaadinSession.class);
        when(session.hasLock()).thenReturn(true);
        StreamResourceRegistry resourceRegistry = new StreamResourceRegistry(session);
        when(session.getResourceRegistry()).thenReturn(resourceRegistry);
        ui.getInternals().setSession(session);
    }

    private static void click(Button button) {
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    private static Stream<Component> descendants(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(RezeptionFlowFilterFrontend_ITest::descendants));
    }
}
