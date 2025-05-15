package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import java.util.Set;
import java.util.UUID;

public interface DiagramService {

    Set<Diagram> findAll(UUID projectId);

    Diagram update(Diagram diagram);

    Diagram update(Diagram diagram, Project project);

    Diagram update(Diagram diagram, Project project, String fileName);

    Diagram create(Project project, String fileName, FileType fileType, DomainModelVisibility visibility, DiagramStylingConfiguration diagramStylingConfiguration);

    void deleteFilesFromFilesystem(String projectId);
}