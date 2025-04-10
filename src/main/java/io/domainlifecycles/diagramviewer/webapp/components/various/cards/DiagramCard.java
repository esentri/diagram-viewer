package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.html.Image;
import io.domainlifecycles.diagramviewer.model.Diagram;

public class DiagramCard extends Card {

    public DiagramCard(final Diagram diagram, final String diagramSrc) {
        setTitle(diagram.getFileName());
        getStyle().setMarginBottom("calc(var(--vaadin-form-layout-column-spacing))");

        Image image = new Image(diagramSrc, diagram.getFileName());
        image.setMaxHeight("200px");
        image.setWidth("95%");
        setMedia(image);
    }
}
