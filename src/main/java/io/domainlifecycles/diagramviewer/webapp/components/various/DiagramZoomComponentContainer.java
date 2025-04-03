package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.orderedlayout.FlexLayout;

public class DiagramZoomComponentContainer extends FlexLayout {

    private DiagramZoomComponent zoomComponent;

    public DiagramZoomComponentContainer() {
        setClassName("outer-zoomist-container");
        setMinHeight("100%");
        setMaxHeight("100%");
        setSizeFull();
    }

    public void reloadZoomComponent(final String projectNameClean, final String diagramName) {
        if(zoomComponent != null) {
            removeAll();
        }

        zoomComponent = new DiagramZoomComponent(projectNameClean, diagramName);

        setFlexGrow(1, zoomComponent);
        add(zoomComponent);
    }
}
