package io.domainlifecycles.diagramviewer.webapp.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.files.DirectoryWatcher;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.sql.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateDiagramDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.GenerateDatabaseModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.UploadDialog;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramViewerView;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Layout
@CssImport("./styles/diagram-viewer-styles.css")
public class MainLayout extends AppLayout {

    private final static Logger log = LoggerFactory.getLogger(MainLayout.class);
    private static final String DLC_LOGO_LOCATION = "frontend/dlc-logo.png";

    private final ProjectService projectService;
    private final DiagramService diagramService;
    private final DomainModelSessionStorage sessionStorage;
    private final UploadDialog uploadDialog;
    private final GenerateDatabaseModelDialog databaseModelDialog;
    private Anchor downloadButton;
    private SideNav sideNav;

    public MainLayout(ProjectService projectService,
        DiagramService diagramService,
        SQLDDLGeneratorService sqlddlGeneratorService,
        DomainModelSessionStorage sessionStorage) {

        this.projectService = projectService;
        this.diagramService = diagramService;
        this.sessionStorage = sessionStorage;
        this.uploadDialog = new UploadDialog(projectService, sessionStorage, this::refreshSideNavLinks);

        this.databaseModelDialog = new GenerateDatabaseModelDialog(sqlddlGeneratorService, sessionStorage);

        sessionStorage.setMainLayout(this);
        addToNavbar(new DrawerToggle(), getDlcLogo(), getDownloadLink(), getDatabaseButton());
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
        return projectService.getAll(buildPath(sessionStorage.getSelectedTargetsDirectory()))
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

    /**
     * Adding a watcher service on the diagram's directory allows an asynchronous refresh of the diagram zoom
     * component as soon as a new diagram has been rendered, for example when some styling option has been changed
     * in the UI.
     *
     * @param absolutePathToDiagram the absolute path to the diagram's directory
     */
    private void initWatcherService(final String absolutePathToDiagram) {
        Path directoryToWatch;

        directoryToWatch = buildPath(absolutePathToDiagram);

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

    private Anchor getDownloadLink() {
        downloadButton = new Anchor(buildDiagramDownloadStreamResource(), "Download Diagram");
        downloadButton.setId("diagramDownloadButton");
        downloadButton.getElement().setAttribute("download", true);
        downloadButton.removeAll();
        downloadButton.setEnabled(sessionStorage.isDiagramSelected());
        downloadButton.add(new Button(new Icon(VaadinIcon.DOWNLOAD_ALT)));
        return downloadButton;
    }

    private StreamResource buildDiagramDownloadStreamResource() {
        if(!sessionStorage.isDiagramSelected()) {
            return null;
        }

        final Diagram selectedDiagram = sessionStorage.getSelectedDiagram();
        return new StreamResource(selectedDiagram.getFileName(), () -> getDiagramFileStream(selectedDiagram.getFullAbsoluteLocationPath()));
    }

    public void updateDownloadLink(boolean buttonEnabled) {
        downloadButton.setHref(buildDiagramDownloadStreamResource());
        downloadButton.setEnabled(buttonEnabled);
    }

    private InputStream getDiagramFileStream(final String diagramLocation) {
        byte[] fileContents = FileIOUtils.readFile(diagramLocation);
        return new ByteArrayInputStream(fileContents);
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

        return new Anchor("/", dlcLogo);
    }

    private static Path buildPath(String absolutePath) {
        Path directoryToWatch;
        try {
            directoryToWatch = Path.of(absolutePath);
        } catch (InvalidPathException e) {
            throw DiagramViewerException.fail(
                String.format("Specified path '%s' is not a directory.", absolutePath), e);
        }
        return directoryToWatch;
    }
}
