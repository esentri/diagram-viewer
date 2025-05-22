package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProjectDomainMirrorServiceImpl implements ProjectDomainMirrorService {

    private final RegenerateDiagramsJobService regenerateDiagramsJobService;
    private final ProjectDomainMirrorRepository repository;
    private final JacksonDomainSerializer serializer;

    public ProjectDomainMirrorServiceImpl(RegenerateDiagramsJobService regenerateDiagramsJobService, ProjectDomainMirrorRepository repository) {
        this.regenerateDiagramsJobService = regenerateDiagramsJobService;
        this.repository = repository;
        this.serializer = new JacksonDomainSerializer(false);
    }

    @Override
    public ProjectDomainMirror getByProjectId(UUID projectId) {
        return repository.findByProjectId(projectId)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No DomainTypeMirror found for project with id '%s'.", projectId)));
    }

    @Override
    public List<DomainTypeMirror> getAllDomainTypeMirrorsWithoutEnumsAndIds(UUID projectId) {
        return repository.findProjectDomainTypesWithOutEnumsAndIds(projectId)
                .stream()
                .map(m -> (DomainTypeMirror)serializer.deserializeTypeMirror(m))
                .toList();
    }

    @Override
    public List<AggregateRootMirror> getAllAggregateRootMirrors(UUID projectId) {
        return repository.findProjectAggregateTypes(projectId)
                .stream()
                .map(m -> (AggregateRootMirror)serializer.deserializeTypeMirror(m))
                .toList();
    }

    @Override
    public ProjectDomainMirror createOrUpdate(Project project, Path projectFilePath, Set<String> domainModelPackages) {
        DomainMirror domainMirror = generateDomainMirror(domainModelPackages, projectFilePath);
        return createOrUpdate(project, domainMirror);
    }

    @Override
    public ProjectDomainMirror createOrUpdate(Project project, DomainMirror domainMirror) {
        Optional<ProjectDomainMirror> foundProjectDomainMirror = repository.findByProjectId(project.getId());

        if(foundProjectDomainMirror.isPresent()) {

            ProjectDomainMirror projectDomainMirror = foundProjectDomainMirror.get();
            projectDomainMirror.setDomainMirror(domainMirror);
            var mirror = repository.save(projectDomainMirror);
            regenerateDiagramsJobService.create(project);
            return mirror;
        }

        ProjectDomainMirror projectDomainMirror = ProjectDomainMirror.builder()
            .projectId(project.getId())
            .domainMirror(domainMirror)
            .build();

        return repository.save(projectDomainMirror);
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
