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
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.plugin.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.sql.NoOpSQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.EditProjectDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.GenerateDatabaseModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.ReuploadDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.ShareProjectDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.cards.DiagramCardGridContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/project/:" + ProjectView.PROJECT_NAME_ROUTE_PARAMETER, layout = MainLayout.class)
@PageTitle("DLC | Project Viewer")
@PermitAll
@Slf4j
public class ProjectView extends FlexLayout implements BeforeEnterObserver {

    public static final String PROJECT_NAME_ROUTE_PARAMETER = "projectName";

    private final SQLDDLGeneratorService sqlddlGeneratorService;
    private final SecurityService securityService;
    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DiagramDirectoryService diagramDirectoryService;
    private final SessionStorage sessionStorage;

    private Project project;
    private String projectName;
    private Registration registration;

    private final boolean jarUploadEnabled;

    public ProjectView(@Value("${jar.upload.enabled}") boolean jarUploadEnabled,
                       SQLDDLGeneratorService sqlddlGeneratorService,
                       SecurityService securityService,
                       ProjectService projectService,
                       DiagramService diagramService,
                       DiagramDirectoryService diagramDirectoryService, SessionStorage sessionStorage) {
        this.jarUploadEnabled = jarUploadEnabled;
        this.sqlddlGeneratorService = sqlddlGeneratorService;
        this.securityService = securityService;
        this.projectService = projectService;
        this.diagramService = diagramService;
        this.diagramDirectoryService = diagramDirectoryService;
        this.sessionStorage = sessionStorage;

        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setId("project-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        log.debug("Enter ProjectView");
        projectName = event.getRouteParameters().get(PROJECT_NAME_ROUTE_PARAMETER).orElseThrow();
        refreshPage();
    }

    private void setProject() {
        project = projectService.getByName(projectName);
    }

    private void addPageContents() {
        add(createAndGetNameAndEditButtonAndReuploadButtonLayout(), createAndGetButtonBar());
        Scroller scroller = new Scroller(new DiagramCardGridContainer(diagramDirectoryService, project, project.getDiagramDirectories(), project.getDiagramsWithoutDirectory()));
        add(scroller);
    }

    private HorizontalLayout createAndGetNameAndEditButtonAndReuploadButtonLayout() {
        EditProjectDialog editProjectDialog = new EditProjectDialog(project, projectService, securityService);
        HorizontalLayout horizontalNameAndEditButtonAndReuploadButtonLayout = new HorizontalLayout();
        Button editProjectButton = new Button(new Icon(VaadinIcon.PENCIL), e -> editProjectDialog.open());
        editProjectButton.addThemeName("icon");
        editProjectButton.getStyle().set("cursor", "pointer");
        editProjectButton.setEnabled(isCurrentlySignedInUserProjectAdmin());

        horizontalNameAndEditButtonAndReuploadButtonLayout.add(new H2(project.getName()), editProjectButton);

        if(jarUploadEnabled){
            ReuploadDialog reuploadDialog = new ReuploadDialog(project, projectService, securityService);
            Button reuploadProjectButton = new Button(new Icon("vaadin:cloud-upload-o"), e -> reuploadDialog.open());
            reuploadProjectButton.addThemeName("icon");
            reuploadProjectButton.getStyle().set("cursor", "pointer");
            reuploadProjectButton.setEnabled(isCurrentlySignedInUserProjectAdmin());
            horizontalNameAndEditButtonAndReuploadButtonLayout.add(reuploadProjectButton);
        }

        horizontalNameAndEditButtonAndReuploadButtonLayout.add(getDeleteProjectButton());
        return horizontalNameAndEditButtonAndReuploadButtonLayout;
    }

    private void refreshPage() {
        setProject();
        removeAll();
        addPageContents();
    }

    private HorizontalLayout createAndGetButtonBar() {
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.getStyle().setMarginTop("2rem");

        buttonBar.add(getCreateDiagramButton());
        if(!(sqlddlGeneratorService instanceof NoOpSQLDDLGeneratorService)){
            buttonBar.add(getDatabaseButton());
        }
        buttonBar.add(getManageUsersButton());

        return buttonBar;
    }

    private Button getCreateDiagramButton() {
        CreateDiagramDialog createDiagramDialog = new CreateDiagramDialog(diagramService, project,
            sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(project.getId()));

        Button createDiagramButton = new Button("Create new Diagram", new Icon("vaadin:plus"));
        createDiagramButton.getStyle().set("cursor", "pointer");

        createDiagramButton.addClickListener(e -> createDiagramDialog.open());
        return createDiagramButton;
    }

    private Button getDatabaseButton() {
        GenerateDatabaseModelDialog databaseModelDialog = new GenerateDatabaseModelDialog(
            sessionStorage,
            sqlddlGeneratorService,
            project
        );

        Button databaseButton = new Button("Download DDL-SQL-Script", new Icon("vaadin:database"));
        databaseButton.getStyle().set("cursor", "pointer");
        databaseButton.addClickListener(e -> databaseModelDialog.open());
        return databaseButton;
    }

    private Button getManageUsersButton() {
        ShareProjectDialog shareProjectDialog = new ShareProjectDialog(projectService, project);

        Button shareProjectButton = new Button("Share Project", new Icon("vaadin:tools"));
        shareProjectButton.getStyle().set("cursor", "pointer");
        shareProjectButton.setEnabled(isCurrentlySignedInUserProjectAdmin());
        shareProjectButton.addClickListener(e -> shareProjectDialog.open());
        return shareProjectButton;
    }

    private Button getDeleteProjectButton() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Project");
        confirmDialog.setText(String.format(
            "Are you sure you want to delete project '%s'?", project.getName()));

        confirmDialog.setCancelable(true);

        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(event -> {
            projectService.delete(project);
            confirmDialog.close();
            UI.getCurrent().navigate(DefaultView.class);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        Button deleteProjectButton = new Button("Delete", new Icon("vaadin:trash"));
        deleteProjectButton.getStyle().set("cursor", "pointer");
        deleteProjectButton.getElement().getStyle().set("margin-left", "auto");
        deleteProjectButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteProjectButton.setEnabled(isCurrentlySignedInUserProjectAdmin());
        deleteProjectButton.addClickListener(e -> confirmDialog.open());
        return deleteProjectButton;
    }

    private boolean isCurrentlySignedInUserProjectAdmin() {
        return Objects.equals(project.getCreator().getId(), securityService.getCurrentlySignedInUser().getId());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration = ComponentUtil.addListener(
                attachEvent.getUI(),
                DiagramsOrProjectsChangedEvent.class,
                event -> refreshPage()
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }
}
