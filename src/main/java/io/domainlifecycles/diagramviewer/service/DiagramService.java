package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;

public interface DiagramService {

    Diagram update(Diagram diagram, Project project);

    Diagram create(Project project, String fileName, String contextPackageName,
                   FileType fileType);

    void deleteFilesFromFilesystem(String projectId);
}