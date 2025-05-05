package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
    public Diagram create(Project project, String fileName, FileType fileType, Set<String> filteredPackages) {
        Path diagramPath = Path.of(diagramsLocation, project.getName(), fileName + fileType.getFileSuffix());

        Diagram diagram = Diagram.builder()
            .fileName(diagramPath.getFileName().toString())
            .fileType(fileType)
                .domainModelVisibility(new DomainModelVisibility(filteredPackages, null, null))

            .project(project)
            .build();

        final Diagram updatedDiagram = insert(diagram);
        createAndSaveDiagramToFilesystem(project, updatedDiagram);

        return updatedDiagram;
    }

    private Diagram insert(Diagram diagram) {
        final String fileName = diagram.getFileName();


        if(diagramNameHasChanged(diagram) && diagramWithNameExists(diagram)) {
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
                project.getDomainMirror(),
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

    private boolean diagramWithNameExists(Diagram diagram) {
        Optional<Diagram> diagramWithName = repository.findByFileName(diagram.getFileName());
        return diagramWithName.isPresent() && Objects.equals(diagram.getFileName(), diagramWithName.get().getFileName());
    }

    private boolean diagramNameHasChanged(Diagram diagram) {
        if(diagram.getId() == null) return false;
        Optional<Diagram> oldDiagram = repository.findById(diagram.getId());
        return oldDiagram.isPresent() && !Objects.equals(oldDiagram.get().getFileName(), diagram.getFileName());
    }
}
