package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.mirror.api.DomainModel;

public interface DiagramService {

    void save(Diagram diagram, DomainModel domainModel);
    Diagram save(Project project, DomainModel domainModel, String fileName, String contextPackageName, FileType fileType);
    void deleteFilesFromFilesystem(String projectNameClean);
}
