package io.domainlifecycles.diagramviewer.webapp.components.various.zoom;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.dom.Style.Overflow;

public class DiagramZoomComponentContainer extends FlexLayout {

    public DiagramZoomComponentContainer(final String projectId, final String diagramName) {
        setMinHeight("100%");
        setMaxHeight("100%");
        setSizeFull();

        getStyle().setMargin("0 1rem 0");
        getStyle().setOverflow(Overflow.HIDDEN);

        DiagramZoomComponent zoomComponent = new DiagramZoomComponent(projectId, diagramName);

        setFlexGrow(1, zoomComponent);
        add(zoomComponent);
    }
}
