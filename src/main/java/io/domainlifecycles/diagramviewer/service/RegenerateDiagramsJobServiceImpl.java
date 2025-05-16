package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.RegenerateDomainMirrorJobRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;

@Service
public class RegenerateDiagramsJobServiceImpl implements RegenerateDiagramsJobService {

    private final RegenerateDomainMirrorJobRepository repository;

    public RegenerateDiagramsJobServiceImpl(RegenerateDomainMirrorJobRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RegenerateDiagramsJob> getAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).toList();
    }

    @Override
    public void create(Project project) {
        RegenerateDiagramsJob job = RegenerateDiagramsJob.builder()
            .project(project)
            .build();

        repository.save(job);
    }

    @Override
    public void delete(RegenerateDiagramsJob job) {
        repository.delete(job);
    }
}
