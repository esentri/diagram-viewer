package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.html.Image;

public class DiagramCard extends Card {

    public DiagramCard(final String diagramName, final String diagramSrc) {
        setTitle(diagramName);
        getStyle().setMarginBottom("calc(var(--vaadin-form-layout-column-spacing))");

        Image image = new Image(diagramSrc, diagramName);
        image.setMaxHeight("200px");
        image.setWidth("95%");
        setMedia(image);
    }
}
