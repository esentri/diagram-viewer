package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.RegenerateDiagramsJobRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegenerateDiagramsJobServiceImpl implements RegenerateDiagramsJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegenerateDiagramsJobServiceImpl.class);

    private final RegenerateDiagramsJobRepository repository;

    public RegenerateDiagramsJobServiceImpl(RegenerateDiagramsJobRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RegenerateDiagramsJob> getAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).toList();
    }

    @Override
    public void create(Project project) {
        project.getDiagrams().forEach(diagram -> {
            RegenerateDiagramsJob job = RegenerateDiagramsJob.builder()
                .diagram(diagram)
                .build();
            repository.save(job);
        });
    }

    @Override
    public void delete(RegenerateDiagramsJob job) {
        repository.delete(job);
    }

    @Override
    public void delete(Diagram diagram) {
        List<RegenerateDiagramsJob> foundJobs = repository.findByDiagramId(diagram.getId());
        foundJobs.forEach(this::delete);
    }
}
