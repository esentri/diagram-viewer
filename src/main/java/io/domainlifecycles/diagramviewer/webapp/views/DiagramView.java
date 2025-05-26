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
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.DiagramTypeNotesDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.RenameDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramVisibilityComponentsContainer;
import io.domainlifecycles.diagramviewer.webapp.components.various.zoom.DiagramZoomComponentContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import jakarta.annotation.security.PermitAll;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/diagram/:" + ProjectView.PROJECT_NAME_ROUTE_PARAMETER + "/:" + DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER, layout = MainLayout.class)
@PageTitle("DLC | Diagram Viewer")
@PermitAll
public class DiagramView extends FlexLayout implements BeforeEnterObserver {

    public static final String DIAGRAM_NAME_ROUTE_PARAMETER = "diagramName";

    private final String diagramsLocation;
    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DiagramTypeNoteService diagramTypeNoteService;
    private final SessionStorage sessionStorage;
    private final SecurityService securityService;
    private String projectName;
    private String diagramName;
    private Project project;
    private Diagram diagram;
    private List<DomainTypeMirror> domainTypeMirrors;
    private Registration registration;

    public DiagramView(
        @Value("${diagrams.location}") String diagramsLocation,
        ProjectService projectService, DiagramService diagramService,
        DiagramTypeNoteService diagramTypeNoteService,
        SessionStorage sessionStorage,
        SecurityService securityService) {

        this.diagramsLocation = diagramsLocation;
        this.projectService = projectService;
        this.diagramService = diagramService;
        this.diagramTypeNoteService = diagramTypeNoteService;
        this.sessionStorage = sessionStorage;
        this.securityService = securityService;

        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setId("diagram-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectName = event.getRouteParameters().get(ProjectView.PROJECT_NAME_ROUTE_PARAMETER).orElseThrow();
        diagramName = event.getRouteParameters().get(DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER).orElseThrow();

        refreshPage();
    }

    private void refreshPage() {
        removeAll();
        setProjectAndDiagramAndDomainTypeMirrors();
        addPageContents();
    }

    private void setProjectAndDiagramAndDomainTypeMirrors() {
        project = projectService.getByName(projectName);
        diagram = project.getDiagrams().stream().filter(foundDiagram ->
                Objects.equals(foundDiagram.getFileName(), diagramName))
            .findAny()
            .orElseThrow(
                () -> DiagramViewerException.fail(String.format("No diagram found with name '%s' .", diagramName)));
        domainTypeMirrors = sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(project.getId());
    }

    private void addPageContents() {
        add(createAndGetButtonBar());

        FlexLayout diagramViewerAndStylingContainer = new FlexLayout();
        diagramViewerAndStylingContainer.setId("diagram-viewer-and-styling-container");
        diagramViewerAndStylingContainer.add(
            new DiagramConfigurationButtonBarComponent(project, diagram, diagramService));
        diagramViewerAndStylingContainer.add(new DiagramZoomComponentContainer(
            project.getId().toString(), diagramName, diagram.getChangedAt(),
            diagram.getDiagramStylingConfiguration().getChangedAt()));

        Scroller scroller = new Scroller(
            new DiagramVisibilityComponentsContainer(sessionStorage, project, diagram, domainTypeMirrors, diagramService));
        scroller.setScrollDirection(ScrollDirection.BOTH);
        scroller.setWidth("30%");
        diagramViewerAndStylingContainer.add(scroller);

        add(diagramViewerAndStylingContainer);
    }

    private HorizontalLayout createAndGetButtonBar() {
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.getStyle().setMarginLeft("3.5rem");
        buttonBar.add(getRenameDiagramButton(), getAddTypeNotesButton(), getDiagramDownloadButton(), getDeleteDiagramButton());
        return buttonBar;
    }

    private Button getRenameDiagramButton() {
        RenameDiagramDialog renameDiagramDialog = new RenameDiagramDialog(diagramService, project, diagram);
        Button renameDiagramButton = new Button("Rename", new Icon(VaadinIcon.PENCIL));
        renameDiagramButton.getStyle().set("cursor", "pointer");
        renameDiagramButton.addClickListener(e -> renameDiagramDialog.open());

        return renameDiagramButton;
    }

    private Anchor getDiagramDownloadButton() {
        Anchor downloadAnchor = new Anchor(buildDiagramDownloadStreamResource(), "Download Diagram");

        downloadAnchor.getStyle().set("cursor", "pointer");
        downloadAnchor.setId("diagramDownloadButton");
        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.removeAll();

        Button downloadDiagramButton = new Button("Download Diagram", new Icon(VaadinIcon.DOWNLOAD_ALT));
        downloadDiagramButton.getStyle().set("cursor", "pointer");
        downloadAnchor.add(downloadDiagramButton);

        return downloadAnchor;
    }

    private Button getAddTypeNotesButton() {
        DiagramTypeNotesDialog diagramTypeNotesDialog = new DiagramTypeNotesDialog(diagram, domainTypeMirrors, diagramTypeNoteService);
        Button typeNotesButton = new Button("Notes", new Icon(VaadinIcon.COMMENT_ELLIPSIS_O));
        typeNotesButton.getStyle().set("cursor", "pointer");
        typeNotesButton.addClickListener(e -> diagramTypeNotesDialog.open());

        return typeNotesButton;
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
            confirmDialog.close();
            UI.getCurrent().navigate(ProjectView.class, new RouteParameters(Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName())));
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        Button deleteDiagramButton = new Button("Delete", new Icon("vaadin:trash"));
        deleteDiagramButton.getStyle().set("cursor", "pointer");
        deleteDiagramButton.getElement().getStyle().set("margin-left", "auto");
        deleteDiagramButton.getElement().getStyle().set("margin-right", "1rem");
        deleteDiagramButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteDiagramButton.setEnabled(
            Objects.equals(project.getCreator().getId(), securityService.getCurrentlySignedInUser().getId()));
        deleteDiagramButton.addClickListener(e -> confirmDialog.open());
        return deleteDiagramButton;
    }

    private StreamResource buildDiagramDownloadStreamResource() {
        Path diagramLocation = Path.of(diagramsLocation, project.getId().toString(), diagram.getFileName());
        byte[] fileContents = FileIOUtils.readFile(diagramLocation.toAbsolutePath().toString());

        return new StreamResource(diagram.getFileName(), () -> new ByteArrayInputStream(fileContents));
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
