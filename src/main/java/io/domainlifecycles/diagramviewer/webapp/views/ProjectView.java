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
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.sql.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.EditProjectDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.GenerateDatabaseModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.ReuploadDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.ShareProjectDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramCardGridContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;

@Route(value = "/:projectName", layout = MainLayout.class)
@PageTitle("DLC | Project Viewer")
@PermitAll
public class ProjectView extends FlexLayout implements BeforeEnterObserver {

    private final SQLDDLGeneratorService sqlddlGeneratorService;
    private final SecurityService securityService;
    private final ProjectService projectService;
    private final DiagramService diagramService;

    private Project project;
    private String projectName;
    private Registration registration;

    public ProjectView(SQLDDLGeneratorService sqlddlGeneratorService, SecurityService securityService, ProjectService projectService, DiagramService diagramService) {
        this.sqlddlGeneratorService = sqlddlGeneratorService;
        this.securityService = securityService;
        this.projectService = projectService;
        this.diagramService = diagramService;

        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setId("project-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectName = event.getRouteParameters().get("projectName").orElseThrow();
        refreshPage();
    }

    private void setProject() {
        project = projectService.getByName(projectName);
    }

    private void addPageContents() {
        add(createAndGetNameAndEditButtonAndReuploadButtonLayout(), createAndGetButtonBar());
        Scroller scroller = new Scroller(new DiagramCardGridContainer(project));
        add(scroller);
    }

    private HorizontalLayout createAndGetNameAndEditButtonAndReuploadButtonLayout() {
        EditProjectDialog editProjectDialog = new EditProjectDialog(project, projectService);
        ReuploadDialog reuploadDialog = new ReuploadDialog(project, projectService);

        HorizontalLayout horizontalNameAndEditButtonAndReuploadButtonLayout = new HorizontalLayout();

        Button editProjectButton = new Button(new Icon("vaadin:pencil"), e -> editProjectDialog.open());
        editProjectButton.addThemeName("icon");
        editProjectButton.getStyle().set("cursor", "pointer");

        Button reuploadProjectButton = new Button(new Icon("vaadin:cloud-upload-o"), e -> reuploadDialog.open());
        reuploadProjectButton.addThemeName("icon");
        editProjectButton.getStyle().set("cursor", "pointer");

        horizontalNameAndEditButtonAndReuploadButtonLayout.add(new H2(project.getName()), editProjectButton, reuploadProjectButton);

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

        buttonBar.add(getCreateDiagramButton(), getDatabaseButton(), getManageUsersButton(), getDeleteProjectButton());

        return buttonBar;
    }

    private Button getCreateDiagramButton() {
        CreateDiagramDialog createDiagramDialog = new CreateDiagramDialog(diagramService, project);

        Button createDiagramButton = new Button("Create new Diagram", new Icon("vaadin:plus"));
        createDiagramButton.getStyle().set("cursor", "pointer");

        createDiagramButton.addClickListener(e -> createDiagramDialog.open());
        return createDiagramButton;
    }

    private Button getDatabaseButton() {
        GenerateDatabaseModelDialog databaseModelDialog = new GenerateDatabaseModelDialog(sqlddlGeneratorService, project);

        Button databaseButton = new Button("Download DDL-SQL-Script", new Icon("vaadin:database"));
        databaseButton.getStyle().set("cursor", "pointer");
        databaseButton.addClickListener(e -> databaseModelDialog.open());
        return databaseButton;
    }

    private Button getManageUsersButton() {
        ShareProjectDialog shareProjectDialog = new ShareProjectDialog(projectService, project);

        Button shareProjectButton = new Button("Share Project", new Icon("vaadin:tools"));
        shareProjectButton.getStyle().set("cursor", "pointer");
        shareProjectButton.setEnabled(Objects.equals(project.getCreator().getId(), securityService.getCurrentlySignedInUser().getId()));
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
        deleteProjectButton.setEnabled(Objects.equals(project.getCreator().getId(), securityService.getCurrentlySignedInUser().getId()));
        deleteProjectButton.addClickListener(e -> confirmDialog.open());
        return deleteProjectButton;
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
