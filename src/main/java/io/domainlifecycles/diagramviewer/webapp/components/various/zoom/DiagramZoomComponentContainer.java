package io.domainlifecycles.diagramviewer.webapp.components.various.zoom;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.dom.Style.Overflow;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import java.time.Instant;

public class DiagramZoomComponentContainer extends FlexLayout {

    public DiagramZoomComponentContainer(final String projectId,
                                         final String diagramName,
                                         final Instant diagramLastModified,
                                         final Instant stylingLastModified) {
        setMinHeight("100%");
        setMaxHeight("100%");
        setSizeFull();

        getStyle().setMargin("0 1rem 0");
        getStyle().setOverflow(Overflow.HIDDEN);

        DiagramZoomComponent zoomComponent =
            new DiagramZoomComponent(
                diagramLastModified, stylingLastModified, projectId, diagramName + DiagramServiceImpl.SVG_FILE_SUFFIX);

        setFlexGrow(1, zoomComponent);
        add(zoomComponent);
    }
}
