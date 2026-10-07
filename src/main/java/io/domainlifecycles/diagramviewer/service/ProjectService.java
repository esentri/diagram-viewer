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

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectService {

    @Transactional(readOnly = true)
    List<Project> getAllAssignedSortedByCreationDate(AppUser appUser);

    Optional<Project> findById(UUID id);

    Project getByName(final String projectName);

    /**
     * Updates the specified project's domainMirror according to the contents in the file under the given path
     * (either in .jar or .json format).
     *
     * @param project the {@link Project} entity to be updated, must not be null
     * @param domainModelPackages a set of domain model package paths associated with the project,
     *                            must not be null
     * @param appUser the {@link AppUser} who is performing the update, must not be null
     * @param pathToFile the file path containing the updated project data, must not be null
     * @param uploadFileType the type of the uploaded file, must not be null and should match supported
     *                       file types
     * @return the updated {@link Project} entity
     */
    @Transactional
    Project updateDomainMirror(Project project, Set<String> domainModelPackages, AppUser appUser, Path pathToFile, UploadFileType uploadFileType);

    /**
     * Creates a new project based on the given parameters.
     *
     * @param projectName the name of the project to be created, must not be null or empty
     * @param domainModelPackages a set of domain model package paths associated with the project, must not be null
     * @param appUser the registered user who is creating the project, must not be null
     * @param pathToFile the file path containing the project's uploaded file, must not be null
     * @param uploadFileType the type of the uploaded file, must not be null and should match supported file types
     * @return the newly created {@link Project} instance
     */
    @Transactional
    Project create(String projectName, Set<String> domainModelPackages, AppUser appUser, Path pathToFile, UploadFileType uploadFileType);

    /**
     * Saves an uploaded domain model for the project with the given name.
     * Could create a new project or alter the domain mirror of an existing one.
     * <p>
     * Only persists: it deliberately does not touch the session scoped {@code SessionStorage}, since an
     * upload is an API request whose session no user ever sees - caching the model there would only keep
     * it in memory until the session times out. Open UI sessions pick up the change on their next access.
     *
     * @param projectName the name of the project to be saved, must not be null or empty
     * @param domainMirrorGz the gzip-compressed JSON of the uploaded domain mirror, already validated, must not be null
     * @param domainCallsGz the gzip-compressed JSON of the static analysis result (DomainCalls) uploaded
     *                      alongside the domain mirror, or {@code null} if none was uploaded
     * @param domainModelPackages the packages the domain model was built from, kept at the project - among others
     *                            to tell declared Bounded Contexts apart from DLC's fallback
     */
    @Transactional
    void createOrUpdateDomainModel(String projectName, byte[] domainMirrorGz, byte[] domainCallsGz,
                                   Collection<String> domainModelPackages);

    /**
     * Renames the specified project with a new name.
     *
     * @param project the {@link Project} entity to be renamed, must not be null
     * @param appUser the user renaming the project, who must be its creator
     * @param newName the new name to assign to the project, must not be null or empty
     * @return the updated {@link Project} entity with the new name
     */
    @Transactional
    Project rename(Project project, AppUser appUser, String newName);

    void assignUser(Project project, String emailAddress);

    void unassignUser(Project project, AppUser appUser);

    @Transactional
    void delete(Project project);

    void deleteDiagram(Project project, Diagram diagram);

    void deleteDiagramDirectory(Project project, DiagramDirectory diagramDirectory);
}