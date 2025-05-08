package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.exception.MirrorException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProjectDomainMirrorServiceImpl implements ProjectDomainMirrorService {

    private final ProjectDomainMirrorRepository repository;

    public ProjectDomainMirrorServiceImpl(ProjectDomainMirrorRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProjectDomainMirror getByProjectId(UUID projectId) {
        return repository.findByProjectId(projectId)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No DomainTypeMirror found for project with id '%s'.", projectId)));
    }

    @Override
    public List<DomainTypeMirror> getAllDomainTypeMirrors(UUID projectId) {
        return getByProjectId(projectId)
            .getDomainMirror().getAllDomainTypeMirrors();
    }

    @Override
    public List<AggregateRootMirror> getAllAggregateRootMirrors(UUID projectId) {
        return getByProjectId(projectId)
            .getDomainMirror().getAllAggregateRootMirrors();
    }

    @Override
    public void createOrUpdate(UUID projectId, Path projectFilePath, Set<String> domainModelPackages) {
        DomainMirror domainMirror = generateDomainMirror(domainModelPackages, projectFilePath);
        createOrUpdate(projectId, domainMirror);
    }

    @Override
    public void createOrUpdate(UUID projectId, DomainMirror domainMirror) {
        Optional<ProjectDomainMirror> foundProjectDomainMirror = repository.findByProjectId(projectId);

        if(foundProjectDomainMirror.isPresent()) {
            ProjectDomainMirror projectDomainMirror = foundProjectDomainMirror.get();
            projectDomainMirror.setDomainMirror(domainMirror);
            repository.save(projectDomainMirror);
            return;
        }

        ProjectDomainMirror projectDomainMirror = ProjectDomainMirror.builder()
            .projectId(projectId)
            .domainMirror(domainMirror)
            .build();

        repository.save(projectDomainMirror);
    }

    @Override
    public void delete(UUID projectId) {
        ProjectDomainMirror projectDomainMirror = getByProjectId(projectId);
        repository.delete(projectDomainMirror);
    }

    private DomainMirror generateDomainMirror(Set<String> domainModelPackages,
                                              Path projectFilePath) {
        return DomainModelUtils.initializeDomainMirrorFromJar(
            projectFilePath,
            domainModelPackages
        );
    }
}
