package io.domainlifecycles.diagramviewer.webapp.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class UserRegisteredEvent extends ComponentEvent<Component> {
    public UserRegisteredEvent(Component source, boolean fromClient) {
        super(source, fromClient);
    }
}
