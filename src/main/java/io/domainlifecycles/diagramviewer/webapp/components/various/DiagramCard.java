package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.html.Image;
import io.domainlifecycles.diagramviewer.webapp.components.various.cards.Card;

public class DiagramCard extends Card {

    public DiagramCard(final String diagramName, final String diagramSrc) {
        setTitle(diagramName);
        Image image = new Image(diagramSrc, diagramName);
        image.setMaxHeight("200px");
        image.setMaxWidth("300px");
        setMedia(image);
    }
}
