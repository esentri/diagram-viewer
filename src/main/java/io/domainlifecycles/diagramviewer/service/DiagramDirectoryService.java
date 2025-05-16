package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

public interface DiagramDirectoryService {

    DiagramDirectory getByName(String name);

    @Transactional
    void create(String name, Project project, Set<Diagram> diagrams);

    @Transactional
    void add(DiagramDirectory diagramDirectory, Diagram diagram);

    void update(DiagramDirectory diagramDirectory, String name);
}
