package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface RegenerateDiagramsJobService {

    List<RegenerateDiagramsJob> getAll();

    @Transactional
    void create(Project project);

    void delete(RegenerateDiagramsJob job);
}
