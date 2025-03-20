package io.domainlifecycles.diagramviewer.webapp.layout;

import com.vaadin.flow.component.Component;
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
import io.domainlifecycles.diagramviewer.sql.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.GenerateDatabaseModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.UploadDialog;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramViewerView;
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
    private final String targetsLocation;
    private final String diagramsDirectory;
    private final UploadDialog uploadDialog;
    private final GenerateDatabaseModelDialog databaseModelDialog;
    private SideNav sideNav;

    public MainLayout(
        @Value("${targets.location}") String targetsLocation,
        @Value("${diagrams.location}") String diagramsDirectory,
        SQLDDLGeneratorService sqlddlGeneratorService,
        AnalyzedDomainModel analyzedDomainModel) {

        this.targetsLocation = targetsLocation;
        this.diagramsDirectory = diagramsDirectory;
        this.uploadDialog = new UploadDialog();
        this.databaseModelDialog = new GenerateDatabaseModelDialog(sqlddlGeneratorService, analyzedDomainModel);

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
        return FileIOUtils.getFileNamesInDirectory(targetsLocation)
            .stream()
            .map(projectName -> {
                SideNavItem parentSideNavItem = new SideNavItem(projectName);
                String projectNameClean = projectName.split("\\.jar")[0];

                FileIOUtils.getFileNamesInDirectory(Path.of(diagramsDirectory, projectNameClean).toString())
                    .forEach(diagramName -> {
                        parentSideNavItem.addItem(
                            new SideNavItem(diagramName, DiagramViewerView.class,
                                new RouteParameters(Map.of("projectName", projectNameClean, "diagramName", diagramName))));

                        initWatcherService(projectNameClean, diagramsDirectory);
                    });

                return parentSideNavItem;
            })
            .toArray(SideNavItem[]::new);
    }

    private void initWatcherService(final String projectName, final String diagramsDirectory) {
        Path directoryToWatch;

        try {
            directoryToWatch = Path.of(diagramsDirectory, projectName);
        } catch (InvalidPathException e) {
            throw DiagramViewerException.fail(
                String.format("Specified path '%s' is not a directory.", diagramsDirectory), e);
        }

        DirectoryWatcher.onDirectoryChange(directoryToWatch,
            (evt) -> {
                log.debug("Noticed change in watched directory. Refreshing Sidenav.");
                this.getUI().ifPresent(ui -> ui.access(this::refreshSideNavLinks));
            }
        );
    }

    private void refreshSideNavLinks() {
        sideNav.removeAll();
        sideNav.addItem(createSideNavLinks());
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
