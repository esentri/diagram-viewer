package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.session.SessionStorage;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiagramServiceImpl implements DiagramService {

    private final String diagramsLocation;
    private final DiagramRepository repository;
    private final SessionStorage sessionStorage;
    private final KrokiClient krokiClient;

    public DiagramServiceImpl(
        @Value("${diagrams.location}") String diagramsLocation,
        DiagramRepository repository,
        SessionStorage sessionStorage,
        KrokiClient krokiClient) {

        this.diagramsLocation = diagramsLocation;
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
    public Diagram save(Project project, String fileName, String contextPackageName, FileType fileType) {
        Path diagramPath = Path.of(diagramsLocation, project.getProjectNameClean(), fileName + fileType.getFileSuffix());

        Diagram diagram = Diagram.builder()
            .fileName(diagramPath.getFileName().toString())
            .fullAbsoluteLocationPath(diagramPath.toAbsolutePath().toString())
            .fileType(fileType)
            .diagramStylingConfiguration(
                DiagramStylingConfiguration.builder()
                    .contextPackageName(contextPackageName)
                    .build())
            .build();

        Diagram persistedDiagram = repository.save(diagram);
        createAndSaveDiagramToFilesystem(project, persistedDiagram);

        return persistedDiagram;
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void createAndSaveDiagramToFilesystem(Project project, Diagram diagram) {
        final String nomnoml = DiagrammerUtils.generateNomnoml(
            sessionStorage.get(project.getId()),
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
