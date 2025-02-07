package io.domainlifecycles.diagramviewer.webapp.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.RouterLink;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramViewerView;
import io.domainlifecycles.diagramviewer.webapp.views.UploadView;

@CssImport("./styles/diagram-viewer-styles.css")
public class MainView extends AppLayout {

    private static final String DLC_LOGO_LOCATION = "frontend/dlc-logo.png";

    public MainView() {
        setPrimarySection(Section.NAVBAR);
        addToNavbar(createNavbarContent());
    }

    private Component createNavbarContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.getStyle().set("backgroundColor", "#0d1f2d");
        layout.setSizeFull();

        Image dlcLogo = new Image(DLC_LOGO_LOCATION, "DLC Logo");
        dlcLogo.setMaxHeight("60px");

        layout.add(dlcLogo, createTabsWithLinks());
        return layout;
    }

    private Tabs createTabsWithLinks() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Orientation.HORIZONTAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createTabLinkItems());
        return tabs;
    }

    private Component[] createTabLinkItems() {
        return new Tab[] {
            createTabLink("Diagram Viewer", DiagramViewerView.class),
            createTabLink("Upload", UploadView.class)
        };
    }

    private Tab createTabLink(String text, Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        var link = new RouterLink(text, navigationTarget);
        tab.add(link);
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }
}
