package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;

public interface DiagramService {

    void save(Diagram diagram);
    Diagram save(Project project, String fileName, String contextPackageName, FileType fileType);
    void delete(Long id);
}
