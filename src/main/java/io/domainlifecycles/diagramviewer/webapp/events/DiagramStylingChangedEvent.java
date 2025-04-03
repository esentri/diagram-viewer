package io.domainlifecycles.diagramviewer.webapp.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class DiagramStylingChangedEvent extends ComponentEvent<Component> {

    public DiagramStylingChangedEvent(Component source, boolean fromClient) {
        super(source, fromClient);
    }
}
