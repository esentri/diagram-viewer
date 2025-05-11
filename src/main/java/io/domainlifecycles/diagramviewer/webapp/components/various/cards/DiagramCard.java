package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Image;
import io.domainlifecycles.diagramviewer.model.Diagram;

public class DiagramCard extends Card implements DragSource<DiagramCard>, DropTarget<DiagramCard>, HasStyle {

    public DiagramCard(final Diagram diagram, final String diagramSrc) {
        setTitle(diagram.getFileName());
        getStyle().setMarginBottom("calc(var(--vaadin-form-layout-column-spacing))");

        Image image = new Image(diagramSrc, diagram.getFileName());
        image.setHeight("200px");
        image.setWidth("95%");
        setMedia(image);
    }
}
