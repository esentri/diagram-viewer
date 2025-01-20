package io.domainlifecycles.diagramviewer.views;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;
import com.flowingcode.vaadin.addons.zoomist.Zoomist.Direction;
import com.flowingcode.vaadin.addons.zoomist.Zoomist.Fill;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.layout.MainView;

@Route(value = "/", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout {

    public DiagramViewerView() {
        this.setMaxWidth("100%");
        this.setMinHeight("0%");
        Zoomist zoomist = getZoomist("images/test.svg");
        this.setFlexGrow(1, zoomist);
        add(zoomist);
    }

    Zoomist getZoomist(String imageSrc) {
        Zoomist zoomist = new Zoomist(imageSrc);
        zoomist.setZoomer(true);
        zoomist.setBounds(false);
        zoomist.setDraggable(true);

        return zoomist;
    }
}
