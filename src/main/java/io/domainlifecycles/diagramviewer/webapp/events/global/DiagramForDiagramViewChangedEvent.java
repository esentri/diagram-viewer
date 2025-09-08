package io.domainlifecycles.diagramviewer.webapp.events.global;

import com.vaadin.flow.component.Component;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import lombok.Getter;

public class DiagramForDiagramViewChangedEvent extends GlobalEvent {

    @Getter
    private final Diagram diagram;

    public DiagramForDiagramViewChangedEvent(Diagram diagram, Component senderComponent) {
        super(senderComponent);
        this.diagram = diagram;
    }
}
