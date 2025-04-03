package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.html.Image;

public class DiagramCard extends Card {

    public DiagramCard(final String diagramName, final String diagramSrc) {
        setTitle(diagramName);
        Image image = new Image(diagramSrc, diagramName);
        image.setMaxHeight("200px");
        image.setMaxWidth("250px");
        setMedia(image);
    }
}
