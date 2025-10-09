package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.model.viewer.User;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectService {

    @Transactional
    Stream<Project> getAll(RegisteredUser registeredUser);

    Optional<Project> findById(UUID id);

    Project getByName(final String projectName);

    /**
     * Updates the specified project's domainMirror according to the contents in the file under the given path
     * (either in .jar or .json format).
     *
     * @param project the {@link Project} entity to be updated, must not be null
     * @param domainModelPackages a set of domain model package paths associated with the project,
     *                            must not be null
     * @param registeredUser the {@link RegisteredUser} who is performing the update, must not be null
     * @param pathToFile the file path containing the updated project data, must not be null
     * @param uploadFileType the type of the uploaded file, must not be null and should match supported
     *                       file types
     * @return the updated {@link Project} entity
     */
    @Transactional
    Project updateDomainMirror(Project project, Set<String> domainModelPackages, RegisteredUser registeredUser, Path pathToFile, UploadFileType uploadFileType);

    /**
     * Creates a new project based on the given parameters.
     *
     * @param projectName the name of the project to be created, must not be null or empty
     * @param domainModelPackages a set of domain model package paths associated with the project, must not be null
     * @param registeredUser the registered user who is creating the project, must not be null
     * @param pathToFile the file path containing the project's uploaded file, must not be null
     * @param uploadFileType the type of the uploaded file, must not be null and should match supported file types
     * @return the newly created {@link Project} instance
     */
    @Transactional
    Project create(String projectName, Set<String> domainModelPackages, RegisteredUser registeredUser, Path pathToFile, UploadFileType uploadFileType);

    /**
     * Saves a project with the given name and domain mirror configuration.
     * Could create a new project or alter the domainMirror of an existing one.
     *
     * @param projectName the name of the project to be saved, must not be null or empty
     * @param domainMirror the domain mirror configuration associated with the project, must not be null
     * @return the saved {@link Project} entity
     */
    @Transactional
    void createOrUpdateDomainModel(String projectName, DomainMirror domainMirror);

    /**
     * Renames the specified project with a new name.
     *
     * @param project the {@link Project} entity to be renamed, must not be null
     * @param newName the new name to assign to the project, must not be null or empty
     * @return the updated {@link Project} entity with the new name
     */
    @Transactional
    Project rename(Project project, RegisteredUser registeredUser, String newName);

    void assignUser(Project project, String emailAddress);

    void assignUser(Project project, User user);

    void unassignUser(Project project, User user);

    @Transactional
    void delete(Project project);

    void deleteDiagram(Project project, Diagram diagram);

    void deleteDiagramDirectory(Project project, DiagramDirectory diagramDirectory);
}