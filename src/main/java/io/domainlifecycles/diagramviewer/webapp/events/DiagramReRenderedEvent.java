package io.domainlifecycles.diagramviewer.webapp.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class DiagramReRenderedEvent extends ComponentEvent<Component> {
    public DiagramReRenderedEvent(Component source, boolean fromClient) {
        super(source, fromClient);
    }
}
