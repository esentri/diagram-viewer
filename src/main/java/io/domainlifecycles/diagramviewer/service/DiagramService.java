package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public interface DiagramService {

    Set<Diagram> findAll(UUID projectId);

    Diagram update(Diagram diagram);

    Diagram update(Diagram diagram, Project project);

    Diagram update(Diagram diagram, Project project, String fileName);

    Diagram create(Project project, String fileName, FileType fileType, Set<String> filteredPackages, Set<String> blacklistedClassnames);

    void deleteFilesFromFilesystem(String projectId);
}