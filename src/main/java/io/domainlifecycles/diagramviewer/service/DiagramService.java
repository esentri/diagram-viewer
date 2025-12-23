package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.util.Set;
import java.util.UUID;

public interface DiagramService {

    Set<Diagram> findAll(UUID projectId);

    Diagram updateModel(Diagram diagram);

    Diagram updateModelAndImage(Diagram diagram);

    Diagram rename(Diagram diagram, String fileName);

    Diagram create(Project project, String fileName, DomainModelVisibility visibility, DiagramStylingConfiguration diagramStylingConfiguration);

    void createAndSaveDiagramToFilesystem(DomainMirror domainMirror, Diagram diagram);

    void deleteFilesFromFilesystem(String projectId);
}