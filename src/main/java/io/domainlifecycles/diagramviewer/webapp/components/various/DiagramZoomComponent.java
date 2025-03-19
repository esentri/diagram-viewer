package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DiagramZoomComponent extends Zoomist {

    public DiagramZoomComponent(String... diagramSrc) {
        super(assembleRequestUrl(diagramSrc) + getDummyRequestParameter());
        setZoomer(true);
        setBounds(false);
        setDraggable(true);
        setWheelable(true);
    }

    private static String assembleRequestUrl(String[] diagramSrc) {
        return String.join("/", diagramSrc);
    }

    private static String getDummyRequestParameter() {
        return "?" + ResourceController.TIMESTAMP_REQUEST_PARAMETER_NAME + "=" + LocalDateTime.now();
    }
}
