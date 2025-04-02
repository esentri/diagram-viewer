package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;
import com.vaadin.flow.dom.Style.Display;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DiagramZoomComponent extends Zoomist {

    public DiagramZoomComponent(String... diagramSrc) {
        super(DiagramFileUtils.assembleDiagramUrl(diagramSrc));
        this.getStyle().setDisplay(Display.FLEX);
        setZoomer(true);
        setBounds(false);
        setDraggable(true);
        setWheelable(true);
    }
}
