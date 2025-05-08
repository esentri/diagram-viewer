package io.domainlifecycles.diagramviewer.webapp.components.various.zoom;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;
import com.vaadin.flow.dom.Style.Display;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import java.time.Instant;

public class DiagramZoomComponent extends Zoomist {

    public DiagramZoomComponent(Instant diagramLastModified, Instant stylingLastModified, String... diagramSrc) {
        super(DiagramFileUtils.assembleDiagramUrl(diagramLastModified, stylingLastModified, diagramSrc));
        this.getStyle().setDisplay(Display.FLEX);
        setZoomer(true);
        setBounds(false);
        setDraggable(true);
        setWheelable(true);
    }
}
