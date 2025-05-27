package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface DiagramTypeNoteService {

    void save(String notes, DomainTypeMirror typeMirror, Diagram diagram);

    String getNotes(Diagram diagram, DomainTypeMirror value);

    Map<String, String> getNotes(Diagram diagram);
}
