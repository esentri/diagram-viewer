package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import io.domainlifecycles.mirror.serialize.Jackson3DomainSerializer;
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
    private final DomainSerializer serializer;

    public ProjectDomainMirrorServiceImpl(RegenerateDiagramsJobService regenerateDiagramsJobService, ProjectDomainMirrorRepository repository) {
        this.regenerateDiagramsJobService = regenerateDiagramsJobService;
        this.repository = repository;
        this.serializer = new Jackson3DomainSerializer(false);
    }

    @Override
    public ProjectDomainMirror getByProjectId(UUID projectId) {
        return repository.findByProjectId(projectId)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No DomainTypeMirror found for project with id '%s'.", projectId)));
    }

    @Override
    public List<DomainTypeMirror> getAllDomainTypeMirrorsWithoutEnumsAndIds(UUID projectId) {
        return repository.findProjectDomainTypesWithoutEnumsAndIds(projectId)
                .stream()
                .map(m -> (DomainTypeMirror)serializer.deserialize(m))
                .toList();
    }

    @Override
    public List<AggregateRootMirror> getAllAggregateRootMirrors(UUID projectId) {
        return repository.findProjectAggregateTypes(projectId)
                .stream()
                .map(m -> (AggregateRootMirror)serializer.deserialize(m))
                .toList();
    }

    @Override
    public ProjectDomainMirror createOrUpdate(Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType) {
        DomainMirror domainMirror = generateDomainMirror(pathToFile, domainModelPackages, uploadFileType);
        return createOrUpdate(project, domainMirror);
    }

    @Override
    public ProjectDomainMirror createOrUpdate(Project project, DomainMirror domainMirror) {
        Optional<ProjectDomainMirror> foundProjectDomainMirror = repository.findByProjectId(project.getId());
        ProjectDomainMirror projectDomainMirror;

        if(foundProjectDomainMirror.isPresent()) {
            projectDomainMirror = foundProjectDomainMirror.get();
            projectDomainMirror.setDomainMirror(domainMirror);
            regenerateDiagramsJobService.create(project);
        }
        else {
            projectDomainMirror = ProjectDomainMirror.builder()
                .projectId(project.getId())
                .domainMirror(domainMirror)
                .build();
        }

        return repository.save(projectDomainMirror);
    }

    @Override
    public void delete(UUID projectId) {
        ProjectDomainMirror projectDomainMirror = getByProjectId(projectId);
        repository.delete(projectDomainMirror);
    }

    private DomainMirror generateDomainMirror(Path pathToJarFile, Set<String> domainModelPackages, UploadFileType uploadFileType) {
        return DomainModelUtils.initializeDomainMirrorFromFile(pathToJarFile, domainModelPackages, uploadFileType);
    }
}
