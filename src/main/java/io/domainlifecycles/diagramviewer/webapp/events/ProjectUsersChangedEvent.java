package io.domainlifecycles.diagramviewer.webapp.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class ProjectUsersChangedEvent extends ComponentEvent<Component> {

    public ProjectUsersChangedEvent(Component source, boolean fromClient) {
        super(source, fromClient);
    }
}
