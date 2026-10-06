package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.BoundedContextAnalysisService;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.sql.NoOpSQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramReRenderedEvent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A diagram created in the project view is rendered in the background: its card shows a placeholder at first and its
 * image as soon as the rendering is done - without reloading the page, and without closing anything else open.
 */
class ProjectViewCardRefreshTest {

    /** held here: Vaadin keeps the current UI only weakly */
    private UI ui;
    private final DiagramService diagramService = mock(DiagramService.class);
    private ProjectView view;

    @BeforeEach
    void openView() {
        ui = new UI();
        UI.setCurrent(ui);

        AppUser user = AppUser.builder().id(UUID.randomUUID()).emailAddress("user@example.com").build();
        Diagram diagram = Diagram.builder().id(UUID.randomUUID()).name("Orders").build();
        Project project = Project.builder().id(UUID.randomUUID()).name("shop").creator(user)
            .diagrams(new HashSet<>(Set.of(diagram))).diagramDirectories(new HashSet<>()).assignedUsers(new HashSet<>(Set.of(user))).build();
        diagram.setProject(project);

        ProjectService projectService = mock(ProjectService.class);
        when(projectService.getByName("shop")).thenReturn(project);
        SecurityService securityService = mock(SecurityService.class);
        when(securityService.getCurrentlySignedInUser()).thenReturn(user);
        when(diagramService.previewLimitBytes()).thenReturn(1024L * 1024);
        // not rendered yet
        when(diagramService.imageSize(any())).thenReturn(-1L);

        view = new ProjectView(false, new NoOpSQLDDLGeneratorService(), securityService, projectService,
            diagramService, mock(DiagramDirectoryService.class), mock(SessionStorage.class),
            mock(BoundedContextAnalysisService.class));
        BeforeEnterEvent enter = mock(BeforeEnterEvent.class);
        when(enter.getRouteParameters()).thenReturn(new RouteParameters(Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, "shop")));
        view.beforeEnter(enter);
        ui.add(view);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void Should_ShowTheImageOfTheCard_When_TheDiagramIsRendered() {
        // given
        assertThat(text()).contains("Not rendered yet");
        assertThat(images()).isEmpty();
        Button createButton = buttons().filter(button -> button.getText().contains("Create new Diagram")).findFirst().orElseThrow();

        // when
        when(diagramService.imageSize(any())).thenReturn(1000L);
        ComponentUtil.fireEvent(ui, new DiagramReRenderedEvent(view, false));

        // then: the card shows the image, the rest of the page stays
        assertThat(text()).doesNotContain("Not rendered yet");
        assertThat(images()).hasSize(1);
        assertThat(buttons()).contains(createButton);
    }

    @Test
    void Should_NotRefreshTheCards_When_TheViewIsDetached() {
        // given
        ui.remove(view);

        // when
        when(diagramService.imageSize(any())).thenReturn(1000L);
        ComponentUtil.fireEvent(ui, new DiagramReRenderedEvent(view, false));

        // then
        assertThat(text()).contains("Not rendered yet");
    }

    private String text() {
        return view.getElement().getTextRecursively();
    }

    private Stream<Element> images() {
        return elements(view.getElement()).filter(element -> !element.isTextNode() && "img".equals(element.getTag()));
    }

    private Stream<Button> buttons() {
        return elements(view.getElement()).flatMap(element -> element.getComponent().stream())
            .filter(Button.class::isInstance).map(Button.class::cast);
    }

    private static Stream<Element> elements(Element root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(ProjectViewCardRefreshTest::elements));
    }
}
