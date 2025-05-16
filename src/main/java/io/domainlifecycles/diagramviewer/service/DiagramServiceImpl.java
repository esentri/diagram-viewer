package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiagramServiceImpl implements DiagramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramServiceImpl.class);

    private final String diagramsLocation;
    private final SessionStorage sessionStorage;
    private final DiagramRepository repository;
    private final KrokiClient krokiClient;

    public DiagramServiceImpl(
        @Value("${diagrams.location}") String diagramsLocation,
        SessionStorage sessionStorage,
        DiagramRepository repository,
        KrokiClient krokiClient) {

        this.diagramsLocation = diagramsLocation;
        this.sessionStorage = sessionStorage;
        this.repository = repository;
        this.krokiClient = krokiClient;
    }

    @Override
    public Set<Diagram> findAll(UUID projectId) {
        return repository.findByProjectId(projectId);
    }

    @Override
    public Diagram update(Diagram diagram) {
        return insert(diagram);
    }

    @Override
    public Diagram update(Diagram diagram, Project project) {
        final Diagram updatedDiagram = insert(diagram);
        createAndSaveDiagramToFilesystem(project, updatedDiagram);

        return updatedDiagram;
    }

    @Override
    public Diagram rename(Diagram diagram, Project project, String fileName) {
        Path diagramPath = Path.of(diagramsLocation, project.getId().toString(), diagram.getFileName());
        String newFilenameWithSuffix = fileName + diagram.getFileType().getFileSuffix();
        FileIOUtils.renameFile(diagramPath, newFilenameWithSuffix);

        diagram.setFileName(newFilenameWithSuffix);
        return update(diagram, project);
    }

    @Override
    public Diagram create(Project project,
                          String fileName,
                          FileType fileType,
                          DomainModelVisibility visibility,
                          DiagramStylingConfiguration diagramStylingConfiguration) {
        Diagram diagram = Diagram.builder()
            .fileName(fileName + fileType.getFileSuffix())
            .fileType(fileType)
            .domainModelVisibility(visibility)
            .diagramStylingConfiguration(diagramStylingConfiguration)
            .project(project)
            .build();

        final Diagram persistedDiagram = insert(diagram);
        createAndSaveDiagramToFilesystem(project, persistedDiagram);

        project.addDiagram(diagram);

        return persistedDiagram;
    }

    @Override
    public void regenerate(Diagram diagram, DomainMirror domainMirror) {
        LOGGER.info(String.format("Regenerating diagram '%s'.", diagram.getFileName()));
        createAndSaveDiagramToFilesystem(diagram.getProject(), diagram, domainMirror);
    }

    private Diagram insert(Diagram diagram) {
        final String fileName = diagram.getFileName();

        if(diagramWithNameExists(diagram) && diagramNameHasChanged(diagram)) {
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
        DomainMirror domainMirror = sessionStorage.getDomainMirror(project.getId());
        createAndSaveDiagramToFilesystem(project, diagram, domainMirror);
    }

    private void createAndSaveDiagramToFilesystem(Project project, Diagram diagram, DomainMirror domainMirror) {
        final String nomnoml;
        try {
            nomnoml = DiagrammerUtils.generateNomnoml(
                domainMirror,
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
        if(diagram.getId() == null) return true;
        Optional<Diagram> oldDiagram = repository.findById(diagram.getId());
        return oldDiagram.isPresent() && !Objects.equals(oldDiagram.get().getFileName(), diagram.getFileName());
    }
}
