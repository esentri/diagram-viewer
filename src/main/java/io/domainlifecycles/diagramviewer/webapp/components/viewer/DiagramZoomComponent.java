package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;

public class DiagramZoomComponent extends Zoomist {

    public DiagramZoomComponent(String diagramSrc) {
        super(diagramSrc);
        setZoomer(true);
        setBounds(false);
        setDraggable(true);
    }
}
