package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import java.util.List;

public interface DiagramService {

    void save(Diagram diagram);
    void save(Project project, String fileName, String contextPackageName, FileType fileType);
}
