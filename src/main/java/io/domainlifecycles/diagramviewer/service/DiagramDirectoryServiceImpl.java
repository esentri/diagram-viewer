package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramDirectoryRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DiagramDirectoryServiceImpl implements DiagramDirectoryService {

    private final DiagramService diagramService;
    private final DiagramDirectoryRepository repository;

    public DiagramDirectoryServiceImpl(DiagramService diagramService, DiagramDirectoryRepository repository) {
        this.diagramService = diagramService;
        this.repository = repository;
    }

    @Override
    public DiagramDirectory getByName(String name) {
        return repository.findByName(name).orElseThrow(() ->
            DiagramViewerException.fail(String.format("No Diagram Directory found with name '%s'.", name)));
    }

    @Override
    public void create(String name, Project project, Set<Diagram> diagrams) {
        DiagramDirectory diagramDirectory = DiagramDirectory.builder()
            .name(name)
            .diagrams(new HashSet<>(diagrams))
            .project(project)
            .build();

        repository.save(diagramDirectory);

        project.addDiagramDirectory(diagramDirectory);
        diagrams.forEach(diagram -> {
            diagram.setDiagramDirectory(diagramDirectory);
            diagramService.update(diagram);
        });
    }

    @Override
    public void add(DiagramDirectory diagramDirectory, Diagram diagram) {
        diagramDirectory.addDiagram(diagram);
        repository.save(diagramDirectory);
        diagramService.update(diagram);
    }

    @Override
    public void update(DiagramDirectory diagramDirectory, String name) {
        diagramDirectory.setName(name);
        repository.save(diagramDirectory);
    }

    @Override
    public void delete(DiagramDirectory diagramDirectory) {
        repository.delete(diagramDirectory);
    }
}
