package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramZoomComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.SimpleDiagramLayout;

@Route(value = "/simple/:diagramName", layout = SimpleDiagramLayout.class)
@PageTitle("DLC | Simple Diagram View")
public class SimpleDiagramView extends Div implements BeforeEnterObserver {

    private String diagramName;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        diagramName = event.getRouteParameters().get("diagramName").orElse("");
        var zoomComponent = new DiagramZoomComponent(diagramName);
        setHeightFull();
        this.add(zoomComponent);
    }
}
