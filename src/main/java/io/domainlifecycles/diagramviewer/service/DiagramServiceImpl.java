package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiagramServiceImpl implements DiagramService {

    private final String diagramsLocation;
    private final ProjectService projectService;
    private final DiagramRepository repository;
    private final DomainModelSessionStorage sessionStorage;
    private final KrokiClient krokiClient;

    public DiagramServiceImpl(
        @Value("${diagrams.location}") String diagramsLocation,
        ProjectService projectService,
        DiagramRepository repository,
        DomainModelSessionStorage sessionStorage,
        KrokiClient krokiClient) {

        this.diagramsLocation = diagramsLocation;
        this.projectService = projectService;
        this.repository = repository;
        this.sessionStorage = sessionStorage;
        this.krokiClient = krokiClient;
    }

    @Override
    public void save(Diagram diagram) {
        Diagram persistedDiagram = repository.save(diagram);
        createAndSaveDiagramToFilesystem(persistedDiagram.getProject(), persistedDiagram);
    }

    @Override
    public void save(Project project, String fileName, String contextPackageName, FileType fileType) {
        Path diagramPath = Path.of(diagramsLocation, project.getProjectNameClean(), fileName + fileType.getFileSuffix());

        Diagram diagram = Diagram.builder()
            .fileName(diagramPath.getFileName().toString())
            .fullAbsoluteLocationPath(diagramPath.toAbsolutePath().toString())
            .project(project)
            .fileType(fileType)
            .diagramStylingConfiguration(
                DiagramStylingConfiguration.builder()
                    .contextPackageName(contextPackageName)
                    .build())
            .build();

        Diagram persistedDiagram = repository.save(diagram);

        project.addDiagram(persistedDiagram);
        projectService.save(project);

        createAndSaveDiagramToFilesystem(project, persistedDiagram);
    }

    private void createAndSaveDiagramToFilesystem(Project project, Diagram diagram) {
        final String nomnoml = DiagrammerUtils.generateNomnoml(
            sessionStorage.get(project.getProjectId()),
            diagram.getDiagramStylingConfiguration(),
            diagram.getDomainModelVisibility());

        byte[] diagramFileContents = krokiClient.convertTo(nomnoml, diagram.getFileType());

        try {
            FileIOUtils.saveFile(diagram.getFullAbsoluteLocationPath(), new ByteArrayInputStream(diagramFileContents));
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not save diagram to '%s'.", diagramsLocation), e);
        }
    }
}
