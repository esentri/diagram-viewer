package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramVisibilityAccordionComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramZoomComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.stream.Collectors.groupingBy;

@Route(value = "/:projectName/:diagramName", layout = MainLayout.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger log = LoggerFactory.getLogger(DiagramViewerView.class);

    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DomainModelSessionStorage sessionStorage;
    private String projectNameClean;
    private String diagramName;
    private Project project;
    private Diagram diagram;
    private FlexLayout zoomComponentContainer;
    private DiagramZoomComponent diagramZoomComponent;

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

        setProjectAndDiagram();
        addPageContents();
        reloadDiagramZoomComponent();
    }

    private void setProjectAndDiagram() {
        project = projectService.getByProjectNameClean(projectNameClean);
        diagram = project.getDiagrams().stream().filter(foundDiagram ->
            Objects.equals(foundDiagram.getFileName(), diagramName))
            .findAny()
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No diagram found with name '%s' .", diagramName)));

        sessionStorage.setSelectedProject(project);
    }

    private void addPageContents() {
        add(new DiagramConfigurationButtonBarComponent(diagram, diagramService, this::reloadDiagramZoomComponent));
        add(createDiagramZoomComponent());
        add(new DiagramVisibilityAccordionComponent(project, diagram, sessionStorage, diagramService, this::reloadDiagramZoomComponent));
    }

    private FlexLayout createDiagramZoomComponent() {
        if(zoomComponentContainer != null) remove(zoomComponentContainer);

        zoomComponentContainer = new FlexLayout();
        zoomComponentContainer.setClassName("outer-zoomist-container");
        zoomComponentContainer.setMinHeight("100%");
        zoomComponentContainer.setMaxHeight("100%");
        zoomComponentContainer.setSizeFull();
        return zoomComponentContainer;
    }

    private void reloadDiagramZoomComponent() {
        if(diagramZoomComponent != null) {
            zoomComponentContainer.remove(diagramZoomComponent);
        }

        diagramZoomComponent = new DiagramZoomComponent(
            ResourceController.RESOURCES_API_PATH, projectNameClean, diagramName);

        zoomComponentContainer.setFlexGrow(1, diagramZoomComponent);
        zoomComponentContainer.add(diagramZoomComponent);
    }
}
