package io.domainlifecycles.diagramviewer.webapp.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class DiagramsChangedEvent extends ComponentEvent<Component> {

    public DiagramsChangedEvent(Component source, boolean fromClient) {
        super(source, fromClient);
    }
}
