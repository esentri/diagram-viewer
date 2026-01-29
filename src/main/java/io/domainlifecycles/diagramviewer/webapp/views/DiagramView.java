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
 *  Copyright 2019-2025 the original author or authors.
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
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.rest.api.ResourceController;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.DownloadDiagramDialog;
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
    private DownloadDiagramDialog downloadDiagramDialog;

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
        downloadDiagramDialog.setDiagram(diagram);
        diagramConfigurationButtonBarComponent.setDiagram(diagram);
        refreshDiagramZoomComponentContainer(diagram);
        buttonBar.removeAll();
        buttonBar.add(
            getRenameDiagramButton(),
            getDiagramDownloadButton(),
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
    }

    private Diagram getDiagram() {
        Project project = projectService.getByName(projectName);
        return project.getDiagrams().stream().filter(foundDiagram ->
                Objects.equals(foundDiagram.getName(), diagramName))
            .findAny()
            .orElseThrow(
                () -> DiagramViewerException.fail(String.format("No diagram found with name '%s' .", diagramName)));
    }

    private void addPageContents() {
        this.buttonBar = createAndGetButtonBar();
        add(buttonBar);

        this.renameDiagramDialog = new RenameDiagramDialog(diagramService);
        add(renameDiagramDialog);

        this.downloadDiagramDialog = new DownloadDiagramDialog(diagramsLocation);
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

    private Button getCopyDiagramLinkButton(Diagram diagram) {
        Button copyDiagramLinkButton = new Button("Copy External Link", new Icon(VaadinIcon.LINK));
        copyDiagramLinkButton.getStyle().set("cursor", "pointer");

        UI.getCurrent().getPage().fetchCurrentURL(url ->
            copyDiagramLinkButton.addClickListener(e -> {
                String baseUrlWithTailingSlash = url.toString();
                baseUrlWithTailingSlash = baseUrlWithTailingSlash.replace("?continue", "");
                String baseUrl = baseUrlWithTailingSlash.substring(0, baseUrlWithTailingSlash.length() - 1);
                String diagramUrl = baseUrl + ResourceController.RESOURCES_API_PATH +
                    ResourceController.VIEW_API_PATH_SUFFIX + "/" + diagram.getProject().getId() + "/" + diagram.getName();
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
