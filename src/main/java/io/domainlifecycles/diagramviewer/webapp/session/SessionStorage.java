package io.domainlifecycles.diagramviewer.webapp.session;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class SessionStorage {

    private final ProjectDomainMirrorService projectDomainMirrorService;
    private final ProjectRepository projectRepository;
    private final Map<UUID, DomainMirrorContainer> domainMirrorContainers;
    @Setter
    @Getter
    private boolean packageFilterOpen;
    private final Map<DomainType, Boolean> domainTypeDiagramSettingsOpen;
    @Setter
    @Getter
    private boolean advancedTrimmingOpen = false;

    public SessionStorage(
            ProjectDomainMirrorService projectDomainMirrorService,
            ProjectRepository projectRepository
    ) {
        this.projectDomainMirrorService = projectDomainMirrorService;
        this.projectRepository = projectRepository;
        this.domainMirrorContainers = new HashMap<>();
        this.domainTypeDiagramSettingsOpen = new HashMap<>();
        this.packageFilterOpen = true;
    }

    public DomainMirror getDomainMirror(UUID projectId) {
        return getDomainMirrorContainer(projectId).getDomainMirror();
    }

    public List<DomainTypeMirror> getAllDomainTypeMirrorsWithoutEnumsAndIds(UUID projectId) {
        return getDomainMirrorContainer(projectId).getDomainTypeMirrors();
    }

    public List<AggregateRootMirror> getAllAggregateRootMirrors(UUID projectId) {
        return getDomainMirrorContainer(projectId).getAggregateRootMirrors();
    }

    private DomainMirrorContainer getDomainMirrorContainer(UUID projectId) {
        var project = projectRepository.findById(projectId);
        if(project.isPresent()) {
            if(domainMirrorContainers.containsKey(projectId)) {
                var container = domainMirrorContainers.get(projectId);

                if(container.getLastUpdated().isBefore(project.get().getLatestChangeInstant())){
                    add(project.get());
                }
                return domainMirrorContainers.get(projectId);
            }
            add(project.get());
            return domainMirrorContainers.get(projectId);
        }
        throw DiagramViewerException.fail("project not found");
    }

    private void add(Project project) {
        var projectDomainMirror = projectDomainMirrorService.getByProjectId(project.getId());
        DomainMirror domainMirror = projectDomainMirror.getDomainMirror();
        List<AggregateRootMirror> aggregateRootMirrors = projectDomainMirrorService.getAllAggregateRootMirrors(
                project.getId());
        List<DomainTypeMirror> domainTypeMirrors = projectDomainMirrorService.getAllDomainTypeMirrorsWithoutEnumsAndIds(
                project.getId());

        DomainMirrorContainer domainMirrorContainer = DomainMirrorContainer.builder()
            .lastUpdated(project.getLatestChangeInstant())
            .domainMirror(domainMirror)
            .aggregateRootMirrors(aggregateRootMirrors)
            .domainTypeMirrors(domainTypeMirrors)
            .build();

        domainMirrorContainers.put(project.getId(), domainMirrorContainer);
    }

    public void createOrUpdate(Project project, DomainMirror domainMirror) {
        ProjectDomainMirror projectDomainMirror = projectDomainMirrorService.createOrUpdate(project, domainMirror);
        createAndAddDomainMirrorContainer(project, projectDomainMirror);
    }

    public void createOrUpdate(Project project, Path projectFilePath, Set<String> domainModelPackages) {
        ProjectDomainMirror projectDomainMirror = projectDomainMirrorService.createOrUpdate(project, projectFilePath,
            domainModelPackages);
        createAndAddDomainMirrorContainer(project, projectDomainMirror);
    }

    public void delete(UUID projectId) {
        projectDomainMirrorService.delete(projectId);
        domainMirrorContainers.remove(projectId);
    }

    private void createAndAddDomainMirrorContainer(Project project, ProjectDomainMirror projectDomainMirror) {
        DomainMirrorContainer domainMirrorContainer = DomainMirrorContainer.builder()
            .lastUpdated(project.getLatestChangeInstant())
            .domainMirror(projectDomainMirror.getDomainMirror())
            .aggregateRootMirrors(projectDomainMirror.getDomainMirror().getAllAggregateRootMirrors())
            .domainTypeMirrors(projectDomainMirror.getDomainMirror().getAllDomainTypeMirrors())
            .build();

        domainMirrorContainers.remove(project.getId());
        domainMirrorContainers.put(project.getId(), domainMirrorContainer);
    }

    public boolean isDomainTypeSettingOpen(DomainType domainType) {
        return domainTypeDiagramSettingsOpen.get(domainType) != null && domainTypeDiagramSettingsOpen.get(domainType);
    }

    public void setDomainTypeSettingOpen(DomainType domainType, boolean open) {
        domainTypeDiagramSettingsOpen.put(domainType, open);
    }

    @Data
    @Builder
    private static class DomainMirrorContainer {
        private Instant lastUpdated;
        private DomainMirror domainMirror;
        private List<AggregateRootMirror> aggregateRootMirrors;
        private List<DomainTypeMirror> domainTypeMirrors;
    }
}
