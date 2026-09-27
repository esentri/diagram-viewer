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

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.RenameDiagramDirectoryDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.cards.DiagramCardGridContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import java.util.UUID;

@Route(value = "/directory/:" + DiagramDirectoryView.DIAGRAM_DIRECTORY_ID_ROUTE_PARAMETER, layout = MainLayout.class)
@PageTitle("DLC | Directory Viewer")
@PermitAll
public class DiagramDirectoryView extends FlexLayout implements BeforeEnterObserver {

    /**
     * Directories are addressed by id: their names are only unique among the sub directories of one parent (e.g.
     * every Bounded Context folder has its own "Commands" folder).
     */
    public static final String DIAGRAM_DIRECTORY_ID_ROUTE_PARAMETER = "diagramDirectoryId";

    private final DiagramDirectoryService diagramDirectoryService;
    private final ProjectService projectService;

    private DiagramDirectory diagramDirectory;
    private Project project;
    private UUID diagramDirectoryId;

    public DiagramDirectoryView(DiagramDirectoryService diagramDirectoryService, ProjectService projectService) {
        this.diagramDirectoryService = diagramDirectoryService;
        this.projectService = projectService;

        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setId("diagram-directory-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        diagramDirectoryId = UUID.fromString(event.getRouteParameters().get(DIAGRAM_DIRECTORY_ID_ROUTE_PARAMETER).orElseThrow());
        refreshPage();
    }

    private void setDiagramDirectoryAndProject() {
        diagramDirectory = diagramDirectoryService.getById(diagramDirectoryId);
        project = diagramDirectory.getProject();
    }

    private void addPageContents() {
        add(createAndGetNameAndDeleteButtonLayout());
        Scroller scroller = new Scroller(new DiagramCardGridContainer(diagramDirectoryService, project,
            project.getSubDirectories(diagramDirectory), diagramDirectory.getDiagrams()));
        add(scroller);
    }

    private void refreshPage() {
        setDiagramDirectoryAndProject();
        removeAll();
        addPageContents();
    }

    private HorizontalLayout createAndGetNameAndDeleteButtonLayout() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new H2(directoryPath()), getRenameDirectoryButton(), getDeleteDirectoryButton());
        return horizontalLayout;
    }

    private String directoryPath() {
        StringBuilder path = new StringBuilder(diagramDirectory.getName());
        for (DiagramDirectory parent = diagramDirectory.getParent(); parent != null; parent = parent.getParent()) {
            path.insert(0, parent.getName() + " / ");
        }
        return path.toString();
    }

    private Button getRenameDirectoryButton() {
        RenameDiagramDirectoryDialog renameDiagramDirectoryDialog = new RenameDiagramDirectoryDialog(diagramDirectoryService, diagramDirectory);

        Button renameDiagramDirectoryButton = new Button(new Icon(VaadinIcon.PENCIL), e -> renameDiagramDirectoryDialog.open());
        renameDiagramDirectoryButton.addThemeName("icon");
        renameDiagramDirectoryButton.getStyle().set("cursor", "pointer");

        return renameDiagramDirectoryButton;
    }

    private Button getDeleteDirectoryButton() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Directory");
        confirmDialog.setText(String.format(
            "Are you sure you want to delete directory '%s' and its sub directories? Their diagrams are kept"
                + " and moved to the project.", diagramDirectory.getName()));

        confirmDialog.setCancelable(true);

        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(event -> {
            projectService.deleteDiagramDirectory(project, diagramDirectory);
            confirmDialog.close();
            UI.getCurrent().navigate(DefaultView.class);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        Button deleteDirectoryButton = new Button("Delete", new Icon("vaadin:trash"));
        deleteDirectoryButton.getStyle().set("cursor", "pointer");
        deleteDirectoryButton.getElement().getStyle().set("margin-left", "auto");
        deleteDirectoryButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteDirectoryButton.addClickListener(e -> confirmDialog.open());
        return deleteDirectoryButton;
    }
}
