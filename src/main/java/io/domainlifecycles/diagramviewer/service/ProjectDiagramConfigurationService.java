package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.model.ProjectConfiguration;
import io.domainlifecycles.diagramviewer.repository.ProjectDiagramConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectDiagramConfigurationService {

    private KrokiClient krokiClient;
    private ProjectDiagramConfigurationRepository repository;

    public ProjectDiagramConfigurationService(ProjectDiagramConfigurationRepository repository) {
        this.repository = repository;
    }

    public ProjectConfiguration getProjectDiagramConfiguration(final String targetName) {
        return repository.getProjectDiagramConfiguration().orElseThrow(() -> DiagramViewerException.fail(String.format("No project found with name: %s", targetName)));
    }
}
