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

import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProjectDomainMirrorService {

    ProjectDomainMirror getByProjectId(final UUID projectId);

    /**
     * Loads the domain mirror of a project, without its static analysis result.
     *
     * @param projectId the project
     * @return the project's domain mirror
     */
    DomainMirror getDomainMirror(final UUID projectId);

    /**
     * Loads the static analysis result ({@code DomainCalls}) of a project, resolved against the given
     * domain mirror of the same project.
     *
     * @param projectId    the project
     * @param domainMirror the project's domain mirror, see {@link #getDomainMirror(UUID)}
     * @return the static analysis result, empty if none was uploaded
     */
    Optional<DomainCalls> loadDomainCalls(final UUID projectId, DomainMirror domainMirror);

    /**
     * Checks whether a static analysis result was uploaded for a project, without loading it.
     *
     * @param projectId the project
     * @return {@code true} if a static analysis result is available
     */
    boolean hasDomainCalls(final UUID projectId);

    /**
     * Creates the domain mirror of a project from an uploaded file (JAR or JSON) and stores it,
     * replacing any previous domain model of the project.
     *
     * @return the created domain mirror
     */
    DomainMirror createOrUpdate(final Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType);

    /**
     * Stores an uploaded domain mirror and static analysis result, given as gzip-compressed JSON,
     * replacing any previous ones of the project - without loading the previous values. Replacing
     * schedules the regeneration of the project's diagrams.
     *
     * @param project        the project
     * @param domainMirrorGz the gzip-compressed JSON of the domain mirror, already validated
     * @param domainCallsGz  the gzip-compressed JSON of the static analysis result, {@code null} if none was uploaded
     */
    void createOrUpdateCompressed(final Project project, byte[] domainMirrorGz, byte[] domainCallsGz);

    void delete(final UUID projectId);
}
