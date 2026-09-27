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

package io.domainlifecycles.diagramviewer.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Keeps the model data of recently used projects in memory, shared by all sessions and the diagram
 * regeneration.
 * <p>
 * Previously every session held its own copy of every project it opened, never releasing it - two users on
 * the same large project meant two full copies. Here each project is held at most once and released
 * {@code projectModelCache.expireAfterAccessMinutes} after its last use.
 * <p>
 * The cache is bounded by memory, not by a number of projects: project sizes differ by
 * orders of magnitude, from a few megabytes to over 300 MB (esprit_2 with its static analysis result). Each entry
 * is weighed by its {@linkplain ProjectModel#estimatedBytes() estimated heap}, and it is weighed again once its
 * static analysis result was loaded on demand. The budget is {@code projectModelCache.maximumMegabytes}, by default
 * half of the maximum heap; least recently used projects are evicted beyond it.
 * <p>
 * An entry is validated against the project's change timestamp on every access and reloaded once the project
 * changed (e.g. by an upload). Loading is atomic per project: concurrent requests for a project that is not
 * cached yet load it only once.
 */
@Component
public class ProjectModelCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectModelCache.class);

    private final ProjectDomainMirrorService projectDomainMirrorService;
    private final ProjectRepository projectRepository;
    private final Cache<UUID, ProjectModel> cache;

    private final long maximumKilobytes;

    /**
     * @param maximumMegabytes         the memory budget of the cache; 0 or less means half of the maximum heap
     * @param expireAfterAccessMinutes how long a project stays cached after its last use
     */
    public ProjectModelCache(ProjectDomainMirrorService projectDomainMirrorService,
                             ProjectRepository projectRepository,
                             @Value("${projectModelCache.maximumMegabytes:0}") long maximumMegabytes,
                             @Value("${projectModelCache.expireAfterAccessMinutes:60}") long expireAfterAccessMinutes) {
        this.projectDomainMirrorService = projectDomainMirrorService;
        this.projectRepository = projectRepository;
        this.maximumKilobytes = maximumMegabytes > 0
            ? maximumMegabytes * 1024
            : Runtime.getRuntime().maxMemory() / 2 / 1024;
        LOGGER.info("Project model cache budget: {} MB", maximumKilobytes / 1024);
        this.cache = Caffeine.newBuilder()
            .maximumWeight(maximumKilobytes)
            .weigher((UUID id, ProjectModel model) -> weightInKilobytes(model))
            .expireAfterAccess(Duration.ofMinutes(expireAfterAccessMinutes))
            .build();
    }

    private static int weightInKilobytes(ProjectModel model) {
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, model.estimatedBytes() / 1024));
    }

    /**
     * Returns the current model data of a project, loading it if it is not cached or the project changed since.
     *
     * @param projectId the project
     * @return the project's model data
     */
    public ProjectModel get(UUID projectId) {
        Instant latestChange = projectRepository.findLatestChange(projectId);
        if (latestChange == null) {
            throw DiagramViewerException.fail("project not found");
        }
        return cache.asMap().compute(projectId, (id, cached) -> {
            if (cached != null && !latestChange.isAfter(cached.lastUpdated())) {
                return cached;
            }
            LOGGER.debug("loading project model of {}", id);
            // only the domain mirror - the static analysis result is loaded on demand, see ProjectModel
            DomainMirror domainMirror = projectDomainMirrorService.getDomainMirror(id);
            return create(id, domainMirror, latestChange, projectDomainMirrorService.hasDomainCalls(id));
        });
    }

    /**
     * Caches the model data of a project whose domain model was just created from an uploaded file (JAR/JSON),
     * which never contains a static analysis result.
     *
     * @param project      the project
     * @param domainMirror the created domain mirror
     */
    public void putFromFileUpload(Project project, DomainMirror domainMirror) {
        cache.put(project.getId(), create(project.getId(), domainMirror, project.getLatestChangeInstant(), false));
    }

    /**
     * Removes a project's model data, e.g. after the project was deleted.
     *
     * @param projectId the project
     */
    public void invalidate(UUID projectId) {
        cache.invalidate(projectId);
    }

    /**
     * @return the number of currently cached projects
     */
    public long size() {
        cache.cleanUp();
        return cache.estimatedSize();
    }

    /**
     * The type lists offered by the view filters are derived from the loaded domain mirror itself: enums and
     * identities are never offered, nor are non-domain classes a diagram cannot
     * show.
     */
    private ProjectModel create(UUID projectId, DomainMirror domainMirror, Instant lastUpdated, boolean domainCallsAvailable) {
        ProjectModel model = new ProjectModel(
            lastUpdated,
            domainMirror,
            domainMirror.getAllAggregateRootMirrors(),
            DomainModelUtils.withoutUnrelatedNonDomainTypes(
                DomainModelUtils.withoutEnumsAndIdentities(domainMirror.getAllDomainTypeMirrors()), domainMirror),
            domainCallsAvailable,
            () -> projectDomainMirrorService.loadDomainCalls(projectId, domainMirror));
        if (model.estimatedBytes() / 1024 > maximumKilobytes) {
            LOGGER.warn("The model of project {} (about {} MB) exceeds the cache budget of {} MB and cannot be kept cached.",
                projectId, model.estimatedBytes() >> 20, maximumKilobytes / 1024);
        }
        // weigh the entry again with its static analysis result - but only if it is still the cached one
        model.onDomainCallsLoaded(loaded -> cache.asMap().replace(projectId, loaded, loaded));
        return model;
    }

    /**
     * @return the summed estimated heap of the currently cached projects, in bytes
     */
    public long estimatedBytes() {
        cache.cleanUp();
        return cache.asMap().values().stream().mapToLong(ProjectModel::estimatedBytes).sum();
    }
}
