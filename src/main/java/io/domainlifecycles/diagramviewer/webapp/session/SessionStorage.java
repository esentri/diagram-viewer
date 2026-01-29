/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2019-2025 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.webapp.session;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@SessionScope
@Slf4j
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
        log.debug("getAllDomainTypeMirrorsWithoutEnumsAndIds for {}", projectId);
        return getDomainMirrorContainer(projectId).getDomainTypeMirrors();
    }

    public List<AggregateRootMirror> getAllAggregateRootMirrors(UUID projectId) {
        log.debug("getAllAggregateRootMirrors for {}", projectId);
        return getDomainMirrorContainer(projectId).getAggregateRootMirrors();
    }

    private DomainMirrorContainer getDomainMirrorContainer(UUID projectId) {
        var ts = projectRepository.findLatestChange(projectId);
        if(ts != null) {
            if(domainMirrorContainers.containsKey(projectId)) {
                DomainMirrorContainer container = domainMirrorContainers.get(projectId);

                Instant containerLastUpdated = container.getLastUpdated();
                if(containerLastUpdated != null && ts.isAfter(containerLastUpdated)){
                    add(projectId, ts);
                }
                log.debug("Returning container for {}", projectId);
                return domainMirrorContainers.get(projectId);
            }
            add(projectId, ts);
            return domainMirrorContainers.get(projectId);
        }
        throw DiagramViewerException.fail("project not found");
    }

    private void add(UUID projectId, Instant latestChange) {
        log.debug("add project {}", projectId);
        var projectDomainMirror = projectDomainMirrorService.getByProjectId(projectId);
        DomainMirror domainMirror = projectDomainMirror.getDomainMirror();
        List<AggregateRootMirror> aggregateRootMirrors = projectDomainMirrorService.getAllAggregateRootMirrors(
                projectId);
        List<DomainTypeMirror> domainTypeMirrors = projectDomainMirrorService.getAllDomainTypeMirrorsWithoutEnumsAndIds(
                projectId);

        DomainMirrorContainer domainMirrorContainer = DomainMirrorContainer.builder()
            .lastUpdated(latestChange)
            .domainMirror(domainMirror)
            .aggregateRootMirrors(aggregateRootMirrors)
            .domainTypeMirrors(domainTypeMirrors)
            .build();

        domainMirrorContainers.put(projectId, domainMirrorContainer);
        log.debug("adding project {} finished", projectId);
    }

    public void createOrUpdate(Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType) {
        ProjectDomainMirror projectDomainMirror = projectDomainMirrorService.createOrUpdate(project, domainModelPackages, pathToFile, uploadFileType);
        createAndAddDomainMirrorContainer(project, projectDomainMirror);
    }

    public void createOrUpdate(Project project, DomainMirror domainMirror) {
        ProjectDomainMirror projectDomainMirror = projectDomainMirrorService.createOrUpdate(project, domainMirror);
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
