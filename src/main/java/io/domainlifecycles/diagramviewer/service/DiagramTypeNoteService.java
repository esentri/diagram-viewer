package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.UUID;

public interface DiagramTypeNoteService {

    void save(String notes, DomainTypeMirror typeMirror, Diagram diagram);

    String getNotes(Diagram diagram, DomainTypeMirror value);
}
