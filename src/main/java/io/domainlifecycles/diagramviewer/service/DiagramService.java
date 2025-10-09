package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.util.Set;
import java.util.UUID;

public interface DiagramService {

    Set<Diagram> findAll(UUID projectId);

    Diagram update(Diagram diagram);

    Diagram updateAndRegenerate(Diagram diagram, Project project);

    Diagram rename(Diagram diagram, Project project, String fileName);

    Diagram create(Project project, String fileName, FileType fileType, DomainModelVisibility visibility, DiagramStylingConfiguration diagramStylingConfiguration);

    void regenerate(Diagram diagram, DomainMirror domainMirror);

    void deleteFilesFromFilesystem(String projectId);
}