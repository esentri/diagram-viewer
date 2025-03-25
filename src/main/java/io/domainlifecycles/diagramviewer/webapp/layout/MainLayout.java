package io.domainlifecycles.diagramviewer.webapp.layout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.files.DirectoryWatcher;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.sql.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.GenerateDatabaseModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.UploadDialog;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramViewerView;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Layout
@CssImport("./styles/diagram-viewer-styles.css")
public class MainLayout extends AppLayout {

    private final static Logger log = LoggerFactory.getLogger(MainLayout.class);

    private static final String DLC_LOGO_LOCATION = "frontend/dlc-logo.png";
    private final Path selectedTargetPath;
    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DomainModelSessionStorage sessionStorage;
    private final UploadDialog uploadDialog;
    private final GenerateDatabaseModelDialog databaseModelDialog;
    private SideNav sideNav;

    public MainLayout(
        @Value("${targets.location}") String defaultTargetsLocation,
        ProjectService projectService,
        DiagramService diagramService,
        SQLDDLGeneratorService sqlddlGeneratorService,
        DomainModelSessionStorage sessionStorage) {

        this.selectedTargetPath = Path.of(defaultTargetsLocation);
        this.projectService = projectService;
        this.diagramService = diagramService;
        this.sessionStorage = sessionStorage;
        this.uploadDialog = new UploadDialog(projectService, defaultTargetsLocation, this::refreshSideNavLinks);

        this.databaseModelDialog = new GenerateDatabaseModelDialog(sqlddlGeneratorService, sessionStorage);

        addToNavbar(new DrawerToggle(), getDlcLogo(), getDatabaseButton());
        buildDrawerContent();
    }

    private void buildDrawerContent() {
        Scroller scroller = new Scroller(getSideNav());
        scroller.setClassName(LumoUtility.Padding.SMALL);

        Button uploadButton = new Button("Upload", new Icon("vaadin:cloud-upload-o"));
        uploadButton.addClickListener(e -> uploadDialog.open());

        addToDrawer(scroller, uploadButton);
    }

    private SideNav getSideNav() {
        sideNav = new SideNav();
        sideNav.addItem(createSideNavLinks());
        return sideNav;
    }

    private SideNavItem[] createSideNavLinks() {
        return projectService.getAll(selectedTargetPath)
            .map(project -> {
                CreateDiagramDialog createDiagramDialog = new CreateDiagramDialog(project, diagramService, this::refreshSideNavLinks);
                SideNavItem parentSideNavItem = new SideNavItem(project.getProjectNameFull());

                Button createDiagramButton = new Button(new Icon("vaadin:plus"));
                createDiagramButton.addClickListener(e -> {
                    sessionStorage.setSelectedProject(project);
                    createDiagramDialog.open();
                });
                parentSideNavItem.setSuffixComponent(createDiagramButton);

                project.getDiagrams()
                    .forEach(diagram -> {
                        SideNavItem sideNavItem = new SideNavItem(diagram.getFileName(), DiagramViewerView.class,
                            new RouteParameters(Map.of("projectName", project.getProjectNameClean(), "diagramName",
                                diagram.getFileName())));

                        parentSideNavItem.addItem(sideNavItem);
                        initWatcherService(diagram.getFullAbsoluteLocationPath());
                    });

                return parentSideNavItem;
            })
            .toArray(SideNavItem[]::new);
    }

    private void initWatcherService(final String absolutePathToDiagram) {
        Path directoryToWatch;

        try {
            directoryToWatch = Path.of(absolutePathToDiagram);
        } catch (InvalidPathException e) {
            throw DiagramViewerException.fail(
                String.format("Specified path '%s' is not a directory.", absolutePathToDiagram), e);
        }

        DirectoryWatcher.onDirectoryChange(directoryToWatch,
            (evt) -> {
                log.debug(String.format("Noticed change in watched diagram's directory '%s'. Refreshing Sidenav.", directoryToWatch));
                this.getUI().ifPresent(ui -> ui.access(this::refreshSideNavLinks));
            }
        );
    }

    private void refreshSideNavLinks() {
        sideNav.removeAll();
        sideNav.addItem(createSideNavLinks());
    }

    private void setNewTargetDirectory(String targetDirectory) {
        projectService.setTargetsDirectory(targetDirectory);
        uploadDialog.setTargetsLocation(targetDirectory);
        refreshSideNavLinks();
    }

    private Button getDatabaseButton() {
        Button databaseButton = new Button(new Icon("vaadin:database"));
        databaseButton.setId("databaseButton");
        databaseButton.addClickListener(e -> databaseModelDialog.open());
        return databaseButton;
    }

    private Component getDlcLogo() {
        Image dlcLogo = new Image(DLC_LOGO_LOCATION, "DLC Logo");
        dlcLogo.setMaxHeight("60px");
        return dlcLogo;
    }
}
