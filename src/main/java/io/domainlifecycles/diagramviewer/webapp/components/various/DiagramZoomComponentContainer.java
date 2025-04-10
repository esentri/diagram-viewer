package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.dom.Style.Overflow;

public class DiagramZoomComponentContainer extends FlexLayout {

    private DiagramZoomComponent zoomComponent;

    public DiagramZoomComponentContainer() {
        setMinHeight("100%");
        setMaxHeight("100%");
        setSizeFull();

        getStyle().setMargin("0 1rem 0");
        getStyle().setOverflow(Overflow.HIDDEN);
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
