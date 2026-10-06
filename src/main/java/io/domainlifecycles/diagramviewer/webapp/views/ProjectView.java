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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import io.domainlifecycles.diagramviewer.service.BoundedContext;
import io.domainlifecycles.diagramviewer.service.BoundedContextAnalysisService;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.sql.NoOpSQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.AnalyzeBoundedContextsDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.BoundedContextAnalysisDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.EditProjectDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.GenerateDatabaseModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.ReuploadDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.ShareProjectDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.cards.DiagramCardGridContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramReRenderedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import io.domainlifecycles.diagramviewer.webapp.rendering.BackgroundDiagramRendering;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import jakarta.annotation.security.PermitAll;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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
    private final BoundedContextAnalysisService boundedContextAnalysisService;

    private Project project;
    private String projectName;
    private Registration registration;
    private Registration renderedRegistration;
    private Scroller cardScroller;

    private final boolean jarUploadEnabled;

    public ProjectView(@Value("${jar.upload.enabled}") boolean jarUploadEnabled,
                       SQLDDLGeneratorService sqlddlGeneratorService,
                       SecurityService securityService,
                       ProjectService projectService,
                       DiagramService diagramService,
                       DiagramDirectoryService diagramDirectoryService, SessionStorage sessionStorage,
                       BoundedContextAnalysisService boundedContextAnalysisService) {
        this.jarUploadEnabled = jarUploadEnabled;
        this.sqlddlGeneratorService = sqlddlGeneratorService;
        this.securityService = securityService;
        this.projectService = projectService;
        this.diagramService = diagramService;
        this.diagramDirectoryService = diagramDirectoryService;
        this.sessionStorage = sessionStorage;
        this.boundedContextAnalysisService = boundedContextAnalysisService;

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
        cardScroller = new Scroller(createCardGrid());
        add(cardScroller);
    }

    private DiagramCardGridContainer createCardGrid() {
        return new DiagramCardGridContainer(diagramDirectoryService, diagramService, project,
            project.getTopLevelDiagramDirectories(), project.getDiagramsWithoutDirectory());
    }

    /**
     * Shows the images of the diagrams rendered in the meantime - only the cards, so that e.g. an open dialog stays.
     */
    private void refreshCards() {
        if (cardScroller == null) {
            return;
        }
        setProject();
        cardScroller.setContent(createCardGrid());
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
        buttonBar.add(getAnalyzeBoundedContextsButton());
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

    /**
     * Creates a directory per Bounded Context with the diagrams of the analyses chosen in a dialog - of its aggregates,
     * their neighborhood, its read models and commands, see {@link BoundedContextAnalysisService}.
     */
    private Button getAnalyzeBoundedContextsButton() {
        List<BoundedContext> boundedContexts = sessionStorage.getBoundedContexts(project.getId());

        Button analyzeButton = new Button("Analyze Bounded Contexts", new Icon(VaadinIcon.SITEMAP));
        // created on click: the dialog shows the analyses checked anew each time; the read model and command diagrams
        // are flows, known from the static analysis result only
        analyzeButton.addClickListener(e -> new AnalyzeBoundedContextsDialog(boundedContexts,
            sessionStorage.hasDomainCalls(project.getId()),
            kinds -> analyzeBoundedContexts(analyzeButton, kinds)).open());

        analyzeButton.setId("analyze-bounded-contexts");
        analyzeButton.getStyle().set("cursor", "pointer");
        return analyzeButton;
    }

    private void analyzeBoundedContexts(Button analyzeButton, Set<BoundedContextAnalysisService.Kind> kinds) {
        UI ui = UI.getCurrent();
        Consumer<Runnable> onUi = BackgroundDiagramRendering.uiUpdater(ui);
        BoundedContextAnalysisDialog progress = new BoundedContextAnalysisDialog();
        progress.open();
        analyzeButton.setEnabled(false);

        boundedContextAnalysisService.analyzeAsync(project, kinds,
                (done, total, diagramName) -> onUi.accept(() -> progress.diagramCreated(done, total, diagramName)))
            .whenComplete((result, error) -> onUi.accept(() -> {
                analyzeButton.setEnabled(true);
                if (error != null) {
                    Throwable cause = error.getCause() != null ? error.getCause() : error;
                    log.error("Analyzing the bounded contexts of project '{}' failed.", project.getName(), cause);
                    progress.failed(String.valueOf(cause.getMessage()));
                    return;
                }
                ComponentUtil.fireEvent(ui, new DiagramsOrProjectsChangedEvent(this, false));
                followRendering(ui, onUi, progress, result);
            }));
    }

    private void followRendering(UI ui, Consumer<Runnable> onUi, BoundedContextAnalysisDialog progress,
                                 BoundedContextAnalysisService.Result result) {
        String summary = String.format("%d Bounded Context(s) analyzed: %d diagram(s) created%s.",
            result.boundedContexts(), result.createdDiagrams(),
            result.skippedDiagrams() > 0 ? String.format(", %d already existed", result.skippedDiagrams()) : "");
        if (result.renderings().isEmpty()) {
            progress.finished(summary);
            return;
        }

        int total = result.renderings().size();
        AtomicInteger rendered = new AtomicInteger();
        progress.diagramRendered(0, total);
        result.renderings().forEach(rendering -> rendering.whenComplete((image, error) -> {
            int done = rendered.incrementAndGet();
            onUi.accept(() -> progress.diagramRendered(done, total));
        }));
        String createdSummary = summary;
        BackgroundDiagramRendering.whenAllRendered(ui, result.renderings(), failed -> {
            // the cards show the images, which are complete only now
            ComponentUtil.fireEvent(ui, new DiagramsOrProjectsChangedEvent(this, false));
            String outcome = createdSummary + (failed == 0
                ? String.format(" All %d diagram(s) rendered.", total)
                : String.format(" %d of %d diagram(s) could not be rendered.", failed, total));
            progress.finished(outcome);
            if (!progress.isOpened()) {
                Notification notification = Notification.show(outcome, 8000, Notification.Position.BOTTOM_END);
                notification.addThemeVariants(failed == 0 ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
            }
        });
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
        // a diagram created here is rendered in the background: its card shows a placeholder until then
        renderedRegistration = ComponentUtil.addListener(
                attachEvent.getUI(),
                DiagramReRenderedEvent.class,
                event -> refreshCards()
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
        renderedRegistration.remove();
    }
}
