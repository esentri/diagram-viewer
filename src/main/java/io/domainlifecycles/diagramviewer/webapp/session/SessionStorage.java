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
 *  Copyright 2025-2026 the original author or authors.
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
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
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
import java.util.Optional;
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
    @Setter
    @Getter
    private boolean flowFilterOpen = false;

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

    /**
     * Returns the static analysis result of a project, loading and deserializing it on first access.
     * <p>
     * The {@code DomainCalls} are only needed for flow filtering, but typically are the largest part of a
     * project's model data - so they are not loaded when a project is opened, only when first requested,
     * and then kept in the session. Use {@link #hasDomainCalls(UUID)} to only check for their presence.
     *
     * @param projectId the project to get the uploaded static analysis result for
     * @return the deserialized {@code DomainCalls} of the given project, empty if none was uploaded
     * alongside its domain mirror
     */
    public Optional<DomainCalls> getDomainCalls(UUID projectId) {
        DomainMirrorContainer container = getDomainMirrorContainer(projectId);
        if (!container.isDomainCallsAvailable()) {
            return Optional.empty();
        }
        if (container.getDomainCalls() == null) {
            log.debug("loading DomainCalls for {}", projectId);
            container.setDomainCalls(projectDomainMirrorService
                .loadDomainCalls(projectId, container.getDomainMirror())
                .orElse(null));
        }
        return Optional.ofNullable(container.getDomainCalls());
    }

    /**
     * @param projectId the project to check
     * @return {@code true} if a static analysis result was uploaded for the given project; does not load it
     */
    public boolean hasDomainCalls(UUID projectId) {
        return getDomainMirrorContainer(projectId).isDomainCallsAvailable();
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
        // only the domain mirror - the static analysis result is loaded on demand, see getDomainCalls
        DomainMirror domainMirror = projectDomainMirrorService.getDomainMirror(projectId);
        domainMirrorContainers.put(projectId,
            createContainer(domainMirror, latestChange, projectDomainMirrorService.hasDomainCalls(projectId)));
        log.debug("adding project {} finished", projectId);
    }

    public void createOrUpdate(Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType) {
        DomainMirror domainMirror = projectDomainMirrorService.createOrUpdate(project, domainModelPackages, pathToFile, uploadFileType);
        // an uploaded file never contains a static analysis result
        domainMirrorContainers.put(project.getId(), createContainer(domainMirror, project.getLatestChangeInstant(), false));
    }

    public void delete(UUID projectId) {
        projectDomainMirrorService.delete(projectId);
        domainMirrorContainers.remove(projectId);
    }

    /**
     * The type lists offered by the view filters are derived from the loaded domain mirror itself. They
     * used to be queried separately via {@code jsonb} (a deliberate optimization back when views loaded
     * them without the full mirror); since the mirror is loaded here anyway, and is stored compressed,
     * deriving them avoids a second, duplicate set of type mirrors (performance plan items 1.3 / 2.4).
     */
    private DomainMirrorContainer createContainer(DomainMirror domainMirror, Instant lastUpdated, boolean domainCallsAvailable) {
        return DomainMirrorContainer.builder()
            .lastUpdated(lastUpdated)
            .domainMirror(domainMirror)
            .aggregateRootMirrors(domainMirror.getAllAggregateRootMirrors())
            .domainTypeMirrors(DomainModelUtils.withoutUnrelatedNonDomainTypes(
                DomainModelUtils.withoutEnumsAndIdentities(domainMirror.getAllDomainTypeMirrors()), domainMirror))
            // loaded on demand, see getDomainCalls
            .domainCallsAvailable(domainCallsAvailable)
            .build();
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
        private boolean domainCallsAvailable;
        /** {@code null} until first requested, see {@link SessionStorage#getDomainCalls(UUID)} */
        private DomainCalls domainCalls;
    }
}
