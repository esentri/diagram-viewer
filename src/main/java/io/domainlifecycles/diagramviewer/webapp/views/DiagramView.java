package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramVisibilityAccordionComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramZoomComponentContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEventListener;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "/:projectName/:diagramName", layout = MainLayout.class)
@PageTitle("DLC | Diagram Viewer")
@PermitAll
public class DiagramView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger log = LoggerFactory.getLogger(DiagramView.class);

    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DomainModelSessionStorage sessionStorage;
    private String projectNameClean;
    private String diagramName;
    private Project project;
    private Diagram diagram;
    private DiagramZoomComponentContainer zoomComponentContainer;

    public DiagramView(DomainModelSessionStorage sessionStorage, ProjectService projectService, DiagramService diagramService) {
        this.sessionStorage = sessionStorage;
        this.projectService = projectService;
        this.diagramService = diagramService;
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
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
        addListener(
            DiagramStylingChangedEvent.class, (DiagramStylingChangedEventListener<DiagramStylingChangedEvent>) changeEvent -> zoomComponentContainer.reloadZoomComponent(projectNameClean, diagramName));
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
        add(getDiagramDownloadButton());

        FlexLayout diagramViewerAndStylingContainer = new FlexLayout();
        diagramViewerAndStylingContainer.setId("diagram-viewer-and-styling-container");
        diagramViewerAndStylingContainer.add(new DiagramConfigurationButtonBarComponent(diagram, diagramService));
        diagramViewerAndStylingContainer.add(createDiagramZoomComponent());
        diagramViewerAndStylingContainer.add(new DiagramVisibilityAccordionComponent(project, diagram, sessionStorage, diagramService));

        add(diagramViewerAndStylingContainer);
    }

    private FlexLayout createDiagramZoomComponent() {
        zoomComponentContainer = new DiagramZoomComponentContainer();
        return zoomComponentContainer;
    }

    private Anchor getDiagramDownloadButton() {
        Anchor downloadAnchor = new Anchor(buildDiagramDownloadStreamResource(), "Download Diagram");
        downloadAnchor.setId("diagramDownloadButton");
        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.removeAll();
        downloadAnchor.add(new Button("Download Diagram", new Icon(VaadinIcon.DOWNLOAD_ALT)));

        return downloadAnchor;
    }

    private StreamResource buildDiagramDownloadStreamResource() {
        if(!sessionStorage.isDiagramSelected()) {
            return null;
        }

        final Diagram selectedDiagram = sessionStorage.getSelectedDiagram();
        return new StreamResource(selectedDiagram.getFileName(), () -> getDiagramFileStream(selectedDiagram.getFullAbsoluteLocationPath()));
    }

    private InputStream getDiagramFileStream(final String diagramLocation) {
        byte[] fileContents = FileIOUtils.readFile(diagramLocation);
        return new ByteArrayInputStream(fileContents);
    }
}
