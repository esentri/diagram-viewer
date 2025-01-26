package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import java.time.LocalDateTime;

public class DiagramZoomComponent extends Zoomist {

    public DiagramZoomComponent(String diagramSrc) {
        super(getDiagramFileSrc(diagramSrc));
        setZoomer(true);
        setBounds(false);
        setDraggable(true);
        setWheelable(true);
    }

    private static String getDiagramFileSrc(String diagramSrc) {
        return diagramSrc + "?" + ResourceController.TIMESTAMP_REQUEST_PARAMETER_NAME + "=" + LocalDateTime.now();
    }
}
