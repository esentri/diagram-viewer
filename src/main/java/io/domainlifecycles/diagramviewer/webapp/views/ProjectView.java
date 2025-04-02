package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramCard;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "/:projectName", layout = MainLayout.class)
@PageTitle("DLC | Project Viewer")
@PermitAll
public class ProjectView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger log = LoggerFactory.getLogger(DiagramView.class);

    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DomainModelSessionStorage sessionStorage;

    private Project project;
    private String projectNameClean;

    public ProjectView(DomainModelSessionStorage sessionStorage, ProjectService projectService, DiagramService diagramService) {
        this.projectService = projectService;
        this.diagramService = diagramService;
        this.sessionStorage = sessionStorage;
        setSizeFull();
        setAlignItems(Alignment.START);
        setClassName("project-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectNameClean = event.getRouteParameters().get("projectName").get();

        setProject();
        addPageContents();
    }

    private void setProject() {
        project = projectService.getByProjectNameClean(projectNameClean);
        sessionStorage.setSelectedProject(project);
    }

    private void addPageContents() {
        add(new DiagramCard("first.svg", assembleDiagramUrl(ResourceController.RESOURCES_API_PATH, projectNameClean, "first.svg")));
    }

    private static String assembleDiagramUrl(String... diagramSrc) {
        return assembleRequestUrl(diagramSrc) + getDummyRequestParameter();
    }

    private static String assembleRequestUrl(String... diagramSrc) {
        return String.join("/", diagramSrc);
    }

    private static String getDummyRequestParameter() {
        return "?" + ResourceController.TIMESTAMP_REQUEST_PARAMETER_NAME + "=" + LocalDateTime.now();
    }
}
