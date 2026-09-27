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

import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.BoundedContext;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.service.ProjectModel;
import io.domainlifecycles.diagramviewer.service.ProjectModelCache;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Session scoped state of the user interface - which filter sections are open and the like.
 * <p>
 * The model data of the projects (domain mirror, type lists, static analysis result) is no longer held here
 * per session, but in the {@link ProjectModelCache} shared by all sessions; the
 * model accessors below only delegate to it, so the views keep working unchanged.
 */
@Component
@SessionScope
@Slf4j
public class SessionStorage {

    private final ProjectDomainMirrorService projectDomainMirrorService;
    private final ProjectModelCache projectModelCache;
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
            ProjectModelCache projectModelCache
    ) {
        this.projectDomainMirrorService = projectDomainMirrorService;
        this.projectModelCache = projectModelCache;
        this.domainTypeDiagramSettingsOpen = new HashMap<>();
        this.packageFilterOpen = true;
    }

    public DomainMirror getDomainMirror(UUID projectId) {
        return projectModelCache.get(projectId).domainMirror();
    }

    /**
     * Returns the static analysis result of a project, loading and deserializing it on first access (see
     * {@link ProjectModel#domainCalls()}). Use {@link #hasDomainCalls(UUID)} to only check for its presence.
     *
     * @param projectId the project to get the uploaded static analysis result for
     * @return the deserialized {@code DomainCalls} of the given project, empty if none was uploaded
     * alongside its domain mirror
     */
    public Optional<DomainCalls> getDomainCalls(UUID projectId) {
        return projectModelCache.get(projectId).domainCalls();
    }

    /**
     * @param projectId the project to check
     * @return {@code true} if a static analysis result was uploaded for the given project; does not load it
     */
    public boolean hasDomainCalls(UUID projectId) {
        return projectModelCache.get(projectId).domainCallsAvailable();
    }

    public List<DomainTypeMirror> getAllDomainTypeMirrorsWithoutEnumsAndIds(UUID projectId) {
        return projectModelCache.get(projectId).domainTypeMirrors();
    }

    /**
     * @param projectId the project
     * @return the Bounded Contexts of the project's domain model, sorted by label
     */
    public List<BoundedContext> getBoundedContexts(UUID projectId) {
        return projectModelCache.get(projectId).boundedContexts();
    }

    /**
     * @param projectId the project
     * @return {@code true} if the project's domain model declares Bounded Contexts (more than DLC's fallback)
     */
    public boolean hasDeclaredBoundedContexts(UUID projectId) {
        return projectModelCache.get(projectId).boundedContextsDeclared();
    }

    public List<AggregateRootMirror> getAllAggregateRootMirrors(UUID projectId) {
        return projectModelCache.get(projectId).aggregateRootMirrors();
    }

    public void createOrUpdate(Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType) {
        DomainMirror domainMirror = projectDomainMirrorService.createOrUpdate(project, domainModelPackages, pathToFile, uploadFileType);
        projectModelCache.putFromFileUpload(project, domainMirror);
    }

    public void delete(UUID projectId) {
        projectDomainMirrorService.delete(projectId);
        projectModelCache.invalidate(projectId);
    }

    public boolean isDomainTypeSettingOpen(DomainType domainType) {
        return domainTypeDiagramSettingsOpen.get(domainType) != null && domainTypeDiagramSettingsOpen.get(domainType);
    }

    public void setDomainTypeSettingOpen(DomainType domainType, boolean open) {
        domainTypeDiagramSettingsOpen.put(domainType, open);
    }
}
