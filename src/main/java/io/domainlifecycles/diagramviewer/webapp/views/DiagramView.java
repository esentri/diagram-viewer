/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.rest.api.ResourceController;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.DownloadDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.RenameDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramVisibilityAndNotesComponentsContainer;
import io.domainlifecycles.diagramviewer.webapp.components.various.zoom.DiagramZoomComponentContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramReRenderedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramRenderingFailedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramRenderingStartedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import jakarta.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Route(value = "/diagram/:" + ProjectView.PROJECT_NAME_ROUTE_PARAMETER + "/:" + DiagramView.DIAGRAM_ROUTE_PARAMETER, layout = MainLayout.class)
@PageTitle("DLC | Diagram Viewer")
@PermitAll
@Slf4j
public class DiagramView extends FlexLayout implements BeforeEnterObserver {

    /**
     * The diagram's id - names are only unique within a directory. A diagram's name is still accepted for links from
     * before, as long as it is unique within the project.
     */
    public static final String DIAGRAM_ROUTE_PARAMETER = "diagram";

    private final String diagramsLocation;
    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DiagramTypeNoteService diagramTypeNoteService;
    private final SessionStorage sessionStorage;
    private final SecurityService securityService;
    private String projectName;
    private String diagramReference;
    private Diagram diagram;

    private FlexLayout diagramViewerAndStylingContainer;
    private DiagramZoomComponentContainer diagramZoomComponentContainer;
    private DiagramVisibilityAndNotesComponentsContainer diagramVisibilityAndNotesComponentsContainer;
    private DiagramConfigurationButtonBarComponent diagramConfigurationButtonBarComponent;
    private HorizontalLayout buttonBar;
    private RenameDiagramDialog renameDiagramDialog;
    private DownloadDiagramDialog downloadDiagramDialog;

    private final ProgressBar renderingProgressBar = new ProgressBar();
    private final VerticalLayout titleLayout = new VerticalLayout();

    private List<Registration> registrations = List.of();

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
        diagramReference = event.getRouteParameters().get(DiagramView.DIAGRAM_ROUTE_PARAMETER).orElseThrow();
        refreshPage();
    }

    private void refreshPage() {
        renderingProgressBar.setVisible(false);
        diagram = getDiagram();
        diagramVisibilityAndNotesComponentsContainer.setDiagram(diagram);
        renameDiagramDialog.setDiagram(diagram);
        downloadDiagramDialog.setDiagram(diagram);
        diagramConfigurationButtonBarComponent.setDiagram(diagram);
        refreshDiagramZoomComponentContainer(diagram);
        buttonBar.removeAll();
        buttonBar.add(
            getRenameDiagramButton(),
            getDiagramDownloadButton(),
            getDeleteDiagramButton(diagram)
        );
    }

    private void refreshDiagramZoomComponentContainer(Diagram diagram) {
        if(this.diagramZoomComponentContainer != null) {
            this.diagramViewerAndStylingContainer.remove(this.diagramZoomComponentContainer);
        }
        this.diagramZoomComponentContainer = new DiagramZoomComponentContainer(
                diagram.getProject().getId().toString(),
                DiagramFileUtils.imageFileName(diagram),
                diagram.getChangedAt(),
                diagram.getDiagramStylingConfiguration().getChangedAt()
        );
        diagramViewerAndStylingContainer.add(diagramZoomComponentContainer);
        diagramViewerAndStylingContainer.setOrder(1, diagramZoomComponentContainer);
        diagramViewerAndStylingContainer.setOrder(2, diagramVisibilityAndNotesComponentsContainer);
        refreshTitle(diagram);
    }

    private Diagram getDiagram() {
        Project project = projectService.getByName(projectName);
        Optional<Diagram> byId = parseId(diagramReference).flatMap(id -> project.getDiagrams().stream()
            .filter(foundDiagram -> id.equals(foundDiagram.getId()))
            .findAny());
        if (byId.isPresent()) {
            return byId.get();
        }
        List<Diagram> byName = project.getDiagrams().stream()
            .filter(foundDiagram -> Objects.equals(foundDiagram.getName(), diagramReference))
            .toList();
        if (byName.size() > 1) {
            throw DiagramViewerException.fail(String.format(
                "The project has %d diagrams named '%s' in different folders - please open it from its folder.",
                byName.size(), diagramReference));
        }
        return byName.stream().findAny().orElseThrow(
            () -> DiagramViewerException.fail(String.format("No diagram found with name '%s' .", diagramReference)));
    }

    private static Optional<UUID> parseId(String reference) {
        try {
            return Optional.of(UUID.fromString(reference));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * @param project the project of the diagram
     * @param diagram a diagram
     * @return the route parameters showing the given diagram
     */
    public static RouteParameters routeParameters(Project project, Diagram diagram) {
        return new RouteParameters(Map.of(
            ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName(),
            DIAGRAM_ROUTE_PARAMETER, diagram.getId().toString()));
    }

    /**
     * The name of the shown diagram above it, below the path of its directory, if it is in one.
     */
    private void refreshTitle(Diagram diagram) {
        titleLayout.removeAll();
        List<String> directories = new ArrayList<>();
        for (DiagramDirectory directory = diagram.getDiagramDirectory(); directory != null; directory = directory.getParent()) {
            directories.add(0, directory.getName());
        }
        if (!directories.isEmpty()) {
            Span path = new Span(String.join(" / ", directories));
            path.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
            titleLayout.add(path);
        }
        H3 title = new H3(diagram.getName());
        title.setId("diagram-title");
        title.getStyle().setMargin("0");
        titleLayout.add(title);
    }

    private void addPageContents() {
        this.buttonBar = createAndGetButtonBar();
        add(buttonBar);

        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);
        titleLayout.getStyle().set("margin", "0.5rem 1rem 0 3.5rem");
        add(titleLayout);

        // shown while the diagram's image is rendered in the background
        renderingProgressBar.setIndeterminate(true);
        renderingProgressBar.setVisible(false);
        renderingProgressBar.setId("diagram-rendering-progress");
        add(renderingProgressBar);

        this.renameDiagramDialog = new RenameDiagramDialog(diagramService);
        add(renameDiagramDialog);

        this.downloadDiagramDialog = new DownloadDiagramDialog(diagramsLocation, sessionStorage, diagramService);
        add(downloadDiagramDialog);

        this.diagramViewerAndStylingContainer = new FlexLayout();
        this.diagramViewerAndStylingContainer.setId("diagram-viewer-and-styling-container");

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
        this.diagramViewerAndStylingContainer.add(diagramVisibilityAndNotesComponentsContainer);
        this.diagramViewerAndStylingContainer.add(diagramConfigurationButtonBarComponent);
        add(this.diagramViewerAndStylingContainer);
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

    private Button getDiagramDownloadButton() {
        Button renameDiagramButton = new Button("Download Diagram", new Icon(VaadinIcon.DOWNLOAD));
        renameDiagramButton.getStyle().set("cursor", "pointer");
        renameDiagramButton.addClickListener(e -> downloadDiagramDialog.open());

        return renameDiagramButton;
    }

    private Button getDeleteDiagramButton(Diagram diagram) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Diagram");
        confirmDialog.setText(String.format(
            "Are you sure you want to delete diagram '%s' from your project?", diagram.getName()));

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
        deleteDiagramButton.addClickListener(e -> confirmDialog.open());
        return deleteDiagramButton;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        registrations = List.of(
            ComponentUtil.addListener(ui, DiagramReRenderedEvent.class, event -> refreshPage()),
            ComponentUtil.addListener(ui, DiagramRenderingStartedEvent.class, event -> renderingProgressBar.setVisible(true)),
            ComponentUtil.addListener(ui, DiagramRenderingFailedEvent.class, event -> renderingProgressBar.setVisible(false))
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registrations.forEach(Registration::remove);
    }

}
