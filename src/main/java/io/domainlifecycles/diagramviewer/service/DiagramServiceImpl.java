package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.mirror.api.DomainModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiagramServiceImpl implements DiagramService {

    private final String diagramsLocation;
    private final DiagramRepository repository;
    private final KrokiClient krokiClient;

    public DiagramServiceImpl(
        @Value("${diagrams.location}") String diagramsLocation,
        DiagramRepository repository,
        KrokiClient krokiClient) {

        this.diagramsLocation = diagramsLocation;
        this.repository = repository;
        this.krokiClient = krokiClient;
    }

    @Override
    public Diagram update(Diagram diagram, DomainModel domainModel) {
        Diagram persistedDiagram = repository.save(diagram);
        createAndSaveDiagramToFilesystem(domainModel, persistedDiagram);
        return persistedDiagram;
    }

    @Override
    public Diagram create(Project project, DomainModel domainModel, String fileName, String contextPackageName, FileType fileType) {
        Path diagramPath = Path.of(diagramsLocation, project.getProjectNameClean(), fileName + fileType.getFileSuffix());

        Diagram diagram = Diagram.builder()
            .fileName(diagramPath.getFileName().toString())
            .fullAbsoluteLocationPath(diagramPath.toAbsolutePath().toString())
            .fileType(fileType)
            .project(project)
            .diagramStylingConfiguration(
                DiagramStylingConfiguration.builder()
                    .contextPackageName(contextPackageName)
                    .build())
            .build();

        Diagram persistedDiagram = repository.save(diagram);
        createAndSaveDiagramToFilesystem(domainModel, persistedDiagram);

        return persistedDiagram;
    }

    @Override
    public void deleteFilesFromFilesystem(String projectNameClean) {
        Path projectDiagramsDirectory = Path.of(diagramsLocation, projectNameClean);

        try {
            FileIOUtils.deleteDirectoryRecursively(projectDiagramsDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createAndSaveDiagramToFilesystem(DomainModel domainModel, Diagram diagram) {
        final String nomnoml = DiagrammerUtils.generateNomnoml(
            domainModel,
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
