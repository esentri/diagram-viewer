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
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramViewerView;
import io.domainlifecycles.diagramviewer.webapp.views.UploadView;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

@Layout
@CssImport("./styles/diagram-viewer-styles.css")
public class MainView extends AppLayout {

    private static final String DLC_LOGO_LOCATION = "frontend/dlc-logo.png";
    private final String targetsLocation;

    public MainView(@Value("${targets.location}") String targetsLocation) {
        this.targetsLocation = targetsLocation;

        addToNavbar(new DrawerToggle(), getDlcLogo());
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
            .map(targetFileName -> new SideNavItem(
                targetFileName,
                DiagramViewerView.class,
                new RouteParameters(
                    new RouteParam( "targetName", targetFileName)
                )))
            .toArray(SideNavItem[]::new);
    }

    private Component getDlcLogo() {
        Image dlcLogo = new Image(DLC_LOGO_LOCATION, "DLC Logo");
        dlcLogo.setMaxHeight("60px");
        return dlcLogo;
    }
}
