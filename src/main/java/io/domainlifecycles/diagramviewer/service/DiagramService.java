package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;

import java.util.Set;

public interface DiagramService {

    Diagram update(Diagram diagram, Project project);

    Diagram create(Project project, String fileName, FileType fileType, Set<String> filteredPackages);

    void deleteFilesFromFilesystem(String projectId);
}