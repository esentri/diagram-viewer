package io.domainlifecycles.diagramviewer.webapp.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class DiagramTypeNotesChangedEvent extends ComponentEvent<Component> {

    private final String typeMirrorName;

    public DiagramTypeNotesChangedEvent(Component source, boolean fromClient, String typeMirrorName) {
        super(source, fromClient);
        this.typeMirrorName = typeMirrorName;
    }

    public String getTypeMirrorName() {
        return typeMirrorName;
    }
}
