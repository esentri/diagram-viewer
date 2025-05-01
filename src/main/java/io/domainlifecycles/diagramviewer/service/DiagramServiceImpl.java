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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
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
    public Diagram update(Diagram diagram, Project project) {
        final Diagram updatedDiagram = insert(diagram);
        createAndSaveDiagramToFilesystem(project, updatedDiagram);

        return updatedDiagram;
    }

    @Override
    public Diagram create(Project project, String fileName, String contextPackageName, FileType fileType) {
        Path diagramPath = Path.of(diagramsLocation, project.getName(), fileName + fileType.getFileSuffix());

        Diagram diagram = Diagram.builder()
            .fileName(diagramPath.getFileName().toString())
            .fileType(fileType)
            .project(project)
            .diagramStylingConfiguration(
                DiagramStylingConfiguration.builder()
                    .contextPackageName(contextPackageName)
                    .build())
            .build();

        final Diagram updatedDiagram = insert(diagram);
        createAndSaveDiagramToFilesystem(project, updatedDiagram);

        return updatedDiagram;
    }

    private Diagram insert(Diagram diagram) {
        final String fileName = diagram.getFileName();
        Optional<Diagram> fetchedDiagram = repository.findByFileName(fileName);

        if(fetchedDiagram.isPresent()) {
            throw DiagramViewerException.fail(String.format("Diagram with name '%s' already exists. Please choose a different name.",
                fileName));
        }

        return repository.save(diagram);
    }

    @Override
    public void deleteFilesFromFilesystem(String projectId) {
        Path projectDiagramsDirectory = Path.of(diagramsLocation, projectId);

        try {
            FileIOUtils.deleteDirectoryRecursively(projectDiagramsDirectory);
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not delete diagrams of project '%s'.", e),
                projectId);
        }
    }

    private void createAndSaveDiagramToFilesystem(Project project, Diagram diagram) {
        final String nomnoml;
        try {
            nomnoml = DiagrammerUtils.generateNomnoml(
                project.getDomainModel(),
                diagram.getDiagramStylingConfiguration(),
                diagram.getDomainModelVisibility());
        } catch(IllegalStateException e) {
            throw DiagramViewerException.fail(e.getMessage(), e);
        }

        byte[] diagramFileContents = krokiClient.convertTo(nomnoml, diagram.getFileType());

        Path diagramPath = Path.of(diagramsLocation, project.getId().toString(), diagram.getFileName());
        try {
            FileIOUtils.saveFile(diagramPath.toAbsolutePath(), new ByteArrayInputStream(diagramFileContents));
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not save diagram to '%s'.", diagramsLocation), e);
        }
    }
}
