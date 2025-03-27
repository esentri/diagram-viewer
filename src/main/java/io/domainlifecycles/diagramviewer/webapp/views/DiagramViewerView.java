package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramVisibilityAccordionComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramZoomComponentContainer;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "/:projectName/:diagramName", layout = MainLayout.class)
@PageTitle("DLC | Diagram Viewer")
@AnonymousAllowed
public class DiagramViewerView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger log = LoggerFactory.getLogger(DiagramViewerView.class);

    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DomainModelSessionStorage sessionStorage;
    private String projectNameClean;
    private String diagramName;
    private Project project;
    private Diagram diagram;
    private DiagramZoomComponentContainer zoomComponentContainer;

    public DiagramViewerView(DomainModelSessionStorage sessionStorage, ProjectService projectService, DiagramService diagramService) {
        this.sessionStorage = sessionStorage;
        this.projectService = projectService;
        this.diagramService = diagramService;
        setSizeFull();
        setClassName("diagram-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectNameClean = event.getRouteParameters().get("projectName").get();
        diagramName = event.getRouteParameters().get("diagramName").get();

        removeAll();
        setProjectAndDiagram();
        addPageContents();
        zoomComponentContainer.reloadZoomComponent(projectNameClean, diagramName);
    }

    private void setProjectAndDiagram() {
        project = projectService.getByProjectNameClean(projectNameClean);
        diagram = project.getDiagrams().stream().filter(foundDiagram ->
            Objects.equals(foundDiagram.getFileName(), diagramName))
            .findAny()
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No diagram found with name '%s' .", diagramName)));

        sessionStorage.setSelectedProject(project);
        sessionStorage.setSelectedDiagram(diagram);
    }

    private void addPageContents() {
        add(new DiagramConfigurationButtonBarComponent(diagram, diagramService, () -> zoomComponentContainer.reloadZoomComponent(projectNameClean, diagramName)));
        add(createDiagramZoomComponent());
        add(new DiagramVisibilityAccordionComponent(project, diagram, sessionStorage, diagramService, () -> zoomComponentContainer.reloadZoomComponent(projectNameClean, diagramName)));
    }

    private FlexLayout createDiagramZoomComponent() {
        zoomComponentContainer = new DiagramZoomComponentContainer();
        return zoomComponentContainer;
    }
}
