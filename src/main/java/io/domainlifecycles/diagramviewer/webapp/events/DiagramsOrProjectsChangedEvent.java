package io.domainlifecycles.diagramviewer.webapp.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class DiagramsOrProjectsChangedEvent extends ComponentEvent<Component> {
    public DiagramsOrProjectsChangedEvent(Component source, boolean fromClient) {
        super(source, fromClient);
    }
}
