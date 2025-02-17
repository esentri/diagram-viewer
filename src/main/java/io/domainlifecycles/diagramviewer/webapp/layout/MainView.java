package io.domainlifecycles.diagramviewer.webapp.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.files.DirectoryWatcher;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramViewerView;
import io.domainlifecycles.diagramviewer.webapp.views.UploadView;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Layout
@CssImport("./styles/diagram-viewer-styles.css")
public class MainView extends AppLayout {

    private final static Logger log = LoggerFactory.getLogger(MainView.class);

    private static final String DLC_LOGO_LOCATION = "frontend/dlc-logo.png";
    private final String targetsLocation;
    private final String diagramsDirectory;

    public MainView(
        @Value("${targets.location}") String targetsLocation,
        @Value("${diagrams.location}") String diagramsDirectory) {

        this.targetsLocation = targetsLocation;
        this.diagramsDirectory = diagramsDirectory;

        addToNavbar(new DrawerToggle(), getDlcLogo());
        buildDrawerContent();
    }

    private void buildDrawerContent() {
        Scroller scroller = new Scroller(getSideNav());
        scroller.setClassName(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
    }

    private SideNav getSideNav() {
        SideNav sideNav = new SideNav();
        sideNav.addItem(createSideNavItems());
        return sideNav;
    }

    private SideNavItem[] createSideNavItems() {
        return FileIOUtils.getFileNamesInDirectory(targetsLocation)
            .stream()
            .map(projectName -> {
                SideNavItem parentSideNavItem = new SideNavItem(projectName);

                FileIOUtils.getFileNamesInDirectory(Path.of(diagramsDirectory, projectName).toString())
                    .forEach(diagramName -> {
                        parentSideNavItem.addItem(
                            new SideNavItem(diagramName, DiagramViewerView.class,
                                new RouteParameters(Map.of("projectName", projectName, "diagramName", diagramName))));

                        initWatcherService(projectName, diagramsDirectory);
                    });

                return parentSideNavItem;
            })
            .toArray(SideNavItem[]::new);
    }

    public void initWatcherService(final String projectName, final String diagramsDirectory) {
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
                this.getUI().ifPresent(ui -> ui.access(this::buildDrawerContent));
            }
        );
    }

    private Component getDlcLogo() {
        Image dlcLogo = new Image(DLC_LOGO_LOCATION, "DLC Logo");
        dlcLogo.setMaxHeight("60px");
        return dlcLogo;
    }
}
