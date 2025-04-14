package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.SessionStorage;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramVisibilityAccordionComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramZoomComponentContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

@Route(value = "/:projectName/:diagramName", layout = MainLayout.class)
@PageTitle("DLC | Diagram Viewer")
@PermitAll
public class DiagramView extends FlexLayout implements BeforeEnterObserver {

    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final SessionStorage sessionStorage;
    private String projectNameClean;
    private String diagramName;
    private Project project;
    private Diagram diagram;
    private Registration registration;

    public DiagramView(SessionStorage sessionStorage, ProjectService projectService, DiagramService diagramService) {
        this.sessionStorage = sessionStorage;
        this.projectService = projectService;
        this.diagramService = diagramService;
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setId("diagram-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectNameClean = event.getRouteParameters().get("projectName").orElseThrow();
        diagramName = event.getRouteParameters().get("diagramName").orElseThrow();

        refreshPage();
    }

    private void refreshPage() {
        removeAll();
        setProjectAndDiagram();
        addPageContents();
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
        add(createAndGetButtonBar());

        FlexLayout diagramViewerAndStylingContainer = new FlexLayout();
        diagramViewerAndStylingContainer.setId("diagram-viewer-and-styling-container");
        diagramViewerAndStylingContainer.add(new DiagramConfigurationButtonBarComponent(project, diagram, diagramService, sessionStorage));
        diagramViewerAndStylingContainer.add(new DiagramZoomComponentContainer(projectNameClean, diagramName));
        diagramViewerAndStylingContainer.add(new DiagramVisibilityAccordionComponent(project, diagram, sessionStorage, diagramService));

        add(diagramViewerAndStylingContainer);
    }

    private HorizontalLayout createAndGetButtonBar() {
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.add(getDiagramDownloadButton(), getDeleteDiagramButton());
        return buttonBar;
    }

    private Anchor getDiagramDownloadButton() {
        Anchor downloadAnchor = new Anchor(buildDiagramDownloadStreamResource(), "Download Diagram");
        downloadAnchor.getStyle().set("cursor", "pointer");
        downloadAnchor.setId("diagramDownloadButton");
        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.getStyle().setMarginLeft("3.5rem");
        downloadAnchor.removeAll();
        downloadAnchor.add(new Button("Download Diagram", new Icon(VaadinIcon.DOWNLOAD_ALT)));

        return downloadAnchor;
    }

    private Button getDeleteDiagramButton() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Diagram");
        confirmDialog.setText(String.format(
            "Are you sure you want to delete diagram '%s' from your project?", diagram.getFileName()));

        confirmDialog.setCancelable(true);

        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(event -> {
            projectService.deleteDiagram(project, diagram);
            sessionStorage.setNoDiagramSelected();
            confirmDialog.close();
            UI.getCurrent().navigate(ProjectView.class, new RouteParameters(Map.of("projectName", project.getProjectNameClean())));
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        Button deleteDiagramButton = new Button("Delete", new Icon("vaadin:trash"));
        deleteDiagramButton.getStyle().set("cursor", "pointer");
        deleteDiagramButton.getElement().getStyle().set("margin-left", "auto");
        deleteDiagramButton.getElement().getStyle().set("margin-right", "1rem");
        deleteDiagramButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteDiagramButton.setEnabled(Objects.equals(project.getCreator().getId(), sessionStorage.getAuthenticatedUser().getId()));
        deleteDiagramButton.addClickListener(e -> confirmDialog.open());
        return deleteDiagramButton;
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration =
            ComponentUtil.addListener(
                attachEvent.getUI(),
                DiagramStylingChangedEvent.class,
                event -> refreshPage()
            );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }
}
