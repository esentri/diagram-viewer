package io.domainlifecycles.diagramviewer.webapp.session;

import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class SessionStorage {

    private final ProjectDomainMirrorService projectDomainMirrorService;
    private final Map<UUID, DomainMirrorContainer> domainMirrorContainers;
    private boolean packageFilterOpen;
    private final Map<DomainType, Boolean> domainTypeDigramSettingsOpen;
    private boolean advancedTrimmingOpen = false;

    public SessionStorage(ProjectDomainMirrorService projectDomainMirrorService) {
        this.projectDomainMirrorService = projectDomainMirrorService;
        this.domainMirrorContainers = new HashMap<>();
        this.domainTypeDigramSettingsOpen = new HashMap<>();
        this.packageFilterOpen = true;
    }

    public DomainMirror getDomainMirror(UUID projectId) {
        if(domainMirrorContainers.containsKey(projectId)) {
            return domainMirrorContainers.get(projectId).getDomainMirror();
        }

        add(projectId);
        return domainMirrorContainers.get(projectId).getDomainMirror();
    }

    public List<DomainTypeMirror> getAllDomainTypeMirrorsWithoutEnumsAndIds(UUID projectId) {
        if(domainMirrorContainers.containsKey(projectId)) {
            return domainMirrorContainers.get(projectId).getDomainTypeMirrors();
        }

        add(projectId);
        return domainMirrorContainers.get(projectId).getDomainTypeMirrors();
    }

    public List<AggregateRootMirror> getAllAggregateRootMirrors(UUID projectId) {
        if(domainMirrorContainers.containsKey(projectId)) {
            return domainMirrorContainers.get(projectId).getAggregateRootMirrors();
        }

        add(projectId);
        return domainMirrorContainers.get(projectId).getAggregateRootMirrors();
    }

    private void add(UUID projectId) {
        DomainMirror domainMirror = projectDomainMirrorService.getByProjectId(projectId).getDomainMirror();
        List<AggregateRootMirror> aggregateRootMirrors = projectDomainMirrorService.getAllAggregateRootMirrors(
            projectId);
        List<DomainTypeMirror> domainTypeMirrors = projectDomainMirrorService.getAllDomainTypeMirrorsWithoutEnumsAndIds(
            projectId);

        DomainMirrorContainer domainMirrorContainer = DomainMirrorContainer.builder()
            .domainMirror(domainMirror)
            .aggregateRootMirrors(aggregateRootMirrors)
            .domainTypeMirrors(domainTypeMirrors)
            .build();

        domainMirrorContainers.put(projectId, domainMirrorContainer);
    }

    public void createOrUpdate(Project project, DomainMirror domainMirror) {
        ProjectDomainMirror projectDomainMirror = projectDomainMirrorService.createOrUpdate(project, domainMirror);
        createAndAddDomainMirrorContainer(project.getId(), projectDomainMirror);
    }

    public void createOrUpdate(Project project, Path projectFilePath, Set<String> domainModelPackages) {
        ProjectDomainMirror projectDomainMirror = projectDomainMirrorService.createOrUpdate(project, projectFilePath,
            domainModelPackages);
        createAndAddDomainMirrorContainer(project.getId(), projectDomainMirror);
    }

    public void delete(UUID projectId) {
        projectDomainMirrorService.delete(projectId);
        domainMirrorContainers.remove(projectId);
    }

    private void createAndAddDomainMirrorContainer(UUID projectId, ProjectDomainMirror projectDomainMirror) {
        DomainMirrorContainer domainMirrorContainer = DomainMirrorContainer.builder()
            .domainMirror(projectDomainMirror.getDomainMirror())
            .aggregateRootMirrors(projectDomainMirror.getDomainMirror().getAllAggregateRootMirrors())
            .domainTypeMirrors(projectDomainMirror.getDomainMirror().getAllDomainTypeMirrors())
            .build();

        domainMirrorContainers.remove(projectId);
        domainMirrorContainers.put(projectId, domainMirrorContainer);
    }

    public boolean isPackageFilterOpen() {
        return packageFilterOpen;
    }

    public void setPackageFilterOpen(boolean packageFilterOpen) {
        this.packageFilterOpen = packageFilterOpen;
    }

    public boolean isDomainTypeSettingOpen(DomainType domainType) {
        return domainTypeDigramSettingsOpen.get(domainType) == null ? false : domainTypeDigramSettingsOpen.get(domainType);
    }

    public void setDomainTypeSettingOpen(DomainType domainType, boolean open) {
        domainTypeDigramSettingsOpen.put(domainType, open);
    }

    public boolean isAdvancedTrimmingOpen() {
        return advancedTrimmingOpen;
    }

    public void setAdvancedTrimmingOpen(boolean advancedTrimmingOpen) {
        this.advancedTrimmingOpen = advancedTrimmingOpen;
    }

    @Data
    @Builder
    private static class DomainMirrorContainer {
        private DomainMirror domainMirror;
        private List<AggregateRootMirror> aggregateRootMirrors;
        private List<DomainTypeMirror> domainTypeMirrors;
    }
}
