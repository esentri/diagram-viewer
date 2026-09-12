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
import io.domainlifecycles.mirror.api.DomainMirror;
import java.nio.file.Path;
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
     * Saves a project with the given name and domain mirror configuration.
     * Could create a new project or alter the domainMirror of an existing one.
     *
     * @param projectName the name of the project to be saved, must not be null or empty
     * @param domainMirror the domain mirror configuration associated with the project, must not be null
     * @param domainCallsJson the raw JSON representation of the static analysis result (DomainCalls) uploaded
     *                        alongside the domain mirror, or {@code null} if none was uploaded
     */
    @Transactional
    void createOrUpdateDomainModel(String projectName, DomainMirror domainMirror, String domainCallsJson);

    /**
     * Renames the specified project with a new name.
     *
     * @param project the {@link Project} entity to be renamed, must not be null
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