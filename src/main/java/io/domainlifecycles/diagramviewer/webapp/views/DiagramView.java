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
import com.vaadin.flow.component.notification.Notification;
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
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.rest.api.ResourceController;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.RenameDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramVisibilityAndNotesComponentsContainer;
import io.domainlifecycles.diagramviewer.webapp.components.various.zoom.DiagramZoomComponentContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Route(value = "/diagram/:" + ProjectView.PROJECT_NAME_ROUTE_PARAMETER + "/:" + DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER, layout = MainLayout.class)
@PageTitle("DLC | Diagram Viewer")
@PermitAll
@Slf4j
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
    private Diagram diagram;


    private FlexLayout diagramViewerAndStylingContainer;
    private DiagramZoomComponentContainer diagramZoomComponentContainer;
    private DiagramVisibilityAndNotesComponentsContainer diagramVisibilityAndNotesComponentsContainer;
    private DiagramConfigurationButtonBarComponent diagramConfigurationButtonBarComponent;
    private HorizontalLayout buttonBar;
    private RenameDiagramDialog renameDiagramDialog;

    private Registration registration;

    public DiagramView(
            @Value("${diagrams.location}") String diagramsLocation,
            ProjectService projectService,
            DiagramService diagramService,
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
        addPageContents();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectName = event.getRouteParameters().get(ProjectView.PROJECT_NAME_ROUTE_PARAMETER).orElseThrow();
        diagramName = event.getRouteParameters().get(DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER).orElseThrow();
        refreshPage();
    }

    private void refreshPage() {
        diagram = getDiagram();
        diagramVisibilityAndNotesComponentsContainer.setDiagram(diagram);
        renameDiagramDialog.setDiagram(diagram);
        diagramConfigurationButtonBarComponent.setDiagram(diagram);
        log.debug("getDiagram finished");
        log.debug("Refreshing diagram view");
        refreshDiagramZoomComponentContainer(diagram);
        log.debug("Refreshing diagram view finished");
        buttonBar.removeAll();
        buttonBar.add(
            getRenameDiagramButton(),
            getDiagramDownloadButton(diagram),
            getCopyDiagramLinkButton(diagram),
            getDeleteDiagramButton(diagram)
        );
    }

    private void refreshDiagramZoomComponentContainer(Diagram diagram) {
        if(this.diagramZoomComponentContainer != null) {
            this.diagramViewerAndStylingContainer.remove(this.diagramZoomComponentContainer);
        }
        this.diagramZoomComponentContainer = new DiagramZoomComponentContainer(
                diagram.getProject().getId().toString(),
                diagramName,
                diagram.getChangedAt(),
                diagram.getDiagramStylingConfiguration().getChangedAt()
        );
        diagramViewerAndStylingContainer.add(diagramZoomComponentContainer);
        diagramViewerAndStylingContainer.setOrder(1, diagramZoomComponentContainer);
        diagramViewerAndStylingContainer.setOrder(2, diagramVisibilityAndNotesComponentsContainer);
        log.debug("creating DiagramZoomComponentContainer finished");
    }

    private Diagram getDiagram() {
        var project = projectService.getByName(projectName);
        var diagram = project.getDiagrams().stream().filter(foundDiagram ->
                Objects.equals(foundDiagram.getFileName(), diagramName))
            .findAny()
            .orElseThrow(
                () -> DiagramViewerException.fail(String.format("No diagram found with name '%s' .", diagramName)));
        return diagram;
    }

    private void addPageContents() {
        log.debug("Add components to diagram view");
        this.buttonBar = createAndGetButtonBar();
        add(buttonBar);
        log.debug("Add Button bar finished");
        renameDiagramDialog = new RenameDiagramDialog(diagramService);
        add(renameDiagramDialog);

        FlexLayout diagramViewerAndStylingContainer = new FlexLayout();
        diagramViewerAndStylingContainer.setId("diagram-viewer-and-styling-container");
        this.diagramViewerAndStylingContainer = diagramViewerAndStylingContainer;
        this.diagramConfigurationButtonBarComponent =
        new DiagramConfigurationButtonBarComponent(
            diagramService
        );
        this.diagramVisibilityAndNotesComponentsContainer =
        new DiagramVisibilityAndNotesComponentsContainer(
            sessionStorage,
            diagramService,
            diagramTypeNoteService
        );
        diagramViewerAndStylingContainer.add(diagramVisibilityAndNotesComponentsContainer);
        log.debug("creating DiagramVisibilityAndNotesComponentsContainer finished");
        diagramViewerAndStylingContainer.add(diagramConfigurationButtonBarComponent);
        log.debug("creating DiagramConfigurationButtonBarComponent finished");
        add(diagramViewerAndStylingContainer);
        log.debug("addPageContents finished");
    }

    private HorizontalLayout createAndGetButtonBar() {
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.getStyle().setMarginLeft("3.5rem");
        return buttonBar;
    }

    private Button getRenameDiagramButton() {
        Button renameDiagramButton = new Button("Rename", new Icon(VaadinIcon.PENCIL));
        renameDiagramButton.getStyle().set("cursor", "pointer");
        renameDiagramButton.addClickListener(e -> renameDiagramDialog.open());

        return renameDiagramButton;
    }

    private Anchor getDiagramDownloadButton(Diagram diagram) {
        Anchor downloadAnchor = new Anchor(buildDiagramDownloadStreamResource(diagram), "Download Diagram");

        downloadAnchor.getStyle().set("cursor", "pointer");
        downloadAnchor.setId("diagramDownloadButton");
        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.removeAll();

        Button downloadDiagramButton = new Button("Download Diagram", new Icon(VaadinIcon.DOWNLOAD_ALT));
        downloadDiagramButton.getStyle().set("cursor", "pointer");
        downloadAnchor.add(downloadDiagramButton);

        return downloadAnchor;
    }

    private Button getCopyDiagramLinkButton(Diagram diagram) {
        Button copyDiagramLinkButton = new Button("Copy External Link", new Icon(VaadinIcon.LINK));
        copyDiagramLinkButton.getStyle().set("cursor", "pointer");

        UI.getCurrent().getPage().fetchCurrentURL(url ->
            copyDiagramLinkButton.addClickListener(e -> {
                String baseUrlWithTailingSlash = url.toString();
                baseUrlWithTailingSlash = baseUrlWithTailingSlash.replace("?continue", "");
                String baseUrl = baseUrlWithTailingSlash.substring(0, baseUrlWithTailingSlash.length() - 1);
                String diagramUrl = baseUrl + ResourceController.RESOURCES_API_PATH +
                    ResourceController.VIEW_API_PATH_SUFFIX + "/" + diagram.getProject().getId() + "/" + diagram.getFileName();
                UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0);", diagramUrl);

                Notification.show("Diagram link has been copied to clipboard. Note: To successfully access the " +
                    "resource, make sure you add your API-Key to the 'X-API-KEY' header in your HTTP request.");
            }));

        return copyDiagramLinkButton;
    }

    private Button getDeleteDiagramButton(Diagram diagram) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Diagram");
        confirmDialog.setText(String.format(
            "Are you sure you want to delete diagram '%s' from your project?", diagram.getFileName()));

        confirmDialog.setCancelable(true);

        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(event -> {
            var project = diagram.getProject();
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
            Objects.equals(diagram.getProject().getCreator().getId(), securityService.getCurrentlySignedInUser().getId()));
        deleteDiagramButton.addClickListener(e -> confirmDialog.open());
        return deleteDiagramButton;
    }

    private StreamResource buildDiagramDownloadStreamResource(Diagram diagram) {
        Path diagramLocation = Path.of(diagramsLocation, diagram.getProject().getId().toString(), diagram.getFileName());
        byte[] fileContents = FileIOUtils.readFile(diagramLocation.toAbsolutePath().toString());

        return new StreamResource(diagram.getFileName(), () -> new ByteArrayInputStream(fileContents));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
            registration = ComponentUtil.addListener(
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
