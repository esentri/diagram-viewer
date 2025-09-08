package io.domainlifecycles.diagramviewer.webapp.events.global;

import com.vaadin.flow.component.Component;
import lombok.Getter;

public abstract class GlobalEvent {

    @Getter
    private final Component senderComponent;

    public GlobalEvent(Component senderComponent) {
        this.senderComponent = senderComponent;
    }
}
