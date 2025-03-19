package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;

import io.domainlifecycles.diagramviewer.rest.ResourceController;

import java.time.LocalDateTime;

public class DiagramZoomComponent extends Zoomist {

    private final String diagramName;

    public DiagramZoomComponent(String diagramName) {
        super(getDiagramFileSrc(diagramName));
        setBounds(false);
        setDraggable(true);
        setWheelable(true);
        this.diagramName = diagramName;
    }

    private static String getDiagramFileSrc(String diagramName) {
        return ResourceController.RESOURCES_API_PATH + "/" + diagramName + "?" + ResourceController.TIMESTAMP_REQUEST_PARAMETER_NAME + "=" + LocalDateTime.now();
    }






}
