package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import java.util.List;
import java.util.UUID;

public interface RegenerateDiagramsJobService {

    List<RegenerateDiagramsJob> getAll();

    void create(Project project);

    void delete(RegenerateDiagramsJob job);
}
