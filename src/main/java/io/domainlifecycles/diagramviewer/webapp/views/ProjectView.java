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
import io.domainlifecycles.diagramviewer.session.SessionStorage;
import io.domainlifecycles.diagramviewer.sql.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.GenerateDatabaseModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.ManageUsersDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramCardGridContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "/:projectName", layout = MainLayout.class)
@PageTitle("DLC | Project Viewer")
@PermitAll
public class ProjectView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProjectView.class);

    private final SQLDDLGeneratorService sqlddlGeneratorService;
    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final SessionStorage sessionStorage;

    private Project project;
    private String projectNameClean;
    private DiagramCardGridContainer diagramCardGridContainer;
    private Registration registration;

    public ProjectView(SQLDDLGeneratorService sqlddlGeneratorService, SessionStorage sessionStorage, ProjectService projectService, DiagramService diagramService) {
        this.sqlddlGeneratorService = sqlddlGeneratorService;
        this.projectService = projectService;
        this.diagramService = diagramService;
        this.sessionStorage = sessionStorage;

        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setId("project-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectNameClean = event.getRouteParameters().get("projectName").get();

        removeAll();
        setProject();
        addPageContents();
        refreshDiagramCardGrid();
    }

    private void setProject() {
        project = projectService.getByProjectNameClean(projectNameClean);
        sessionStorage.setSelectedProject(project);
    }

    private void addPageContents() {
        add(new H2(projectNameClean));
        add(createAndGetButtonBar());

        diagramCardGridContainer = new DiagramCardGridContainer();
        Scroller scroller = new Scroller(diagramCardGridContainer);
        add(scroller);
    }

    private void refreshDiagramCardGrid() {
        setProject();
        diagramCardGridContainer.reloadDiagramCardGrid(project);
    }

    private HorizontalLayout createAndGetButtonBar() {
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.getStyle().setMarginTop("2rem");

        buttonBar.add(getCreateDiagramButton(), getDatabaseButton(), getManageUsersButton(), getDeleteProjectButton());

        return buttonBar;
    }

    private Button getCreateDiagramButton() {
        CreateDiagramDialog createDiagramDialog = new CreateDiagramDialog(project, diagramService);

        Button createDiagramButton = new Button("Create new Diagram", new Icon("vaadin:plus"));
        createDiagramButton.getStyle().set("cursor", "pointer");

        createDiagramButton.addClickListener(e -> {
            sessionStorage.setSelectedProject(project);
            createDiagramDialog.open();
        });
        return createDiagramButton;
    }

    private Button getDatabaseButton() {
        GenerateDatabaseModelDialog databaseModelDialog = new GenerateDatabaseModelDialog(sqlddlGeneratorService, sessionStorage);

        Button databaseButton = new Button("Download DDL-SQL-Script", new Icon("vaadin:database"));
        databaseButton.getStyle().set("cursor", "pointer");
        databaseButton.addClickListener(e -> databaseModelDialog.open());
        return databaseButton;
    }

    private Button getManageUsersButton() {
        ManageUsersDialog manageUsersDialog = new ManageUsersDialog(projectService, sessionStorage, project);

        Button manageUsersButton = new Button("Manage Users", new Icon("vaadin:tools"));
        manageUsersButton.getStyle().set("cursor", "pointer");
        manageUsersButton.setEnabled(Objects.equals(project.getCreator().getId(), sessionStorage.getAuthenticatedUser().getId()));
        manageUsersButton.addClickListener(e -> manageUsersDialog.open());
        return manageUsersButton;
    }

    private Button getDeleteProjectButton() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Project");
        confirmDialog.setText(String.format(
            "Are you sure you want to delete project '%s'", project.getProjectNameClean()));

        confirmDialog.setCancelable(true);

        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(event -> {
            projectService.delete(project);
            sessionStorage.setNoneSelected();
            confirmDialog.close();
            UI.getCurrent().navigate(DefaultView.class);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        Button deleteProjectButton = new Button("Delete", new Icon("vaadin:trash"));
        deleteProjectButton.getStyle().set("cursor", "pointer");
        deleteProjectButton.getElement().getStyle().set("margin-left", "auto");
        deleteProjectButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteProjectButton.setEnabled(Objects.equals(project.getCreator().getId(), sessionStorage.getAuthenticatedUser().getId()));
        deleteProjectButton.addClickListener(e -> confirmDialog.open());
        return deleteProjectButton;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration = ComponentUtil.addListener(
                attachEvent.getUI(),
                DiagramsOrProjectsChangedEvent.class,
                event -> {
                    refreshDiagramCardGrid();
                    sessionStorage.refreshAuthenticatedUser();
                }
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }
}
