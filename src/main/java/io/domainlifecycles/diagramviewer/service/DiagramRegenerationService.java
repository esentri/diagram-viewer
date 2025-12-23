package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.mirror.api.DomainMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DiagramRegenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramRegenerationService.class);

    private final ProjectDomainMirrorService projectDomainMirrorService;

    private final DiagramService diagramService;

    public DiagramRegenerationService(
            ProjectDomainMirrorService projectDomainMirrorService,
            DiagramService diagramService
    ) {
        this.projectDomainMirrorService = projectDomainMirrorService;
        this.diagramService = diagramService;
    }

    public void regenerate(Diagram diagram) {
        LOGGER.info(String.format("Regenerating diagram '%s'.", diagram.getName()));
        var projectDomainMirror = projectDomainMirrorService.getByProjectId(diagram.getProject().getId());
        DomainMirror domainMirror = projectDomainMirror.getDomainMirror();
        diagramService.createAndSaveDiagramToFilesystem(domainMirror, diagram);
    }
}
