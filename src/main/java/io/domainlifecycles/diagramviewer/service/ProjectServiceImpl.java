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

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final String diagramsLocation;
    private final DiagramService diagramService;
    private final DiagramTypeNoteService diagramTypeNoteService;
    private final RegenerateDiagramsJobService regenerateDiagramsJobService;
    private final AppUserService appUserService;
    private final SessionStorage sessionStorage;
    private final ProjectDomainMirrorService projectDomainMirrorService;
    private final ProjectRepository repository;

    public ProjectServiceImpl(
        @Value("${diagrams.location}") String diagramsLocation,
        DiagramService diagramService,
        DiagramTypeNoteService diagramTypeNoteService, RegenerateDiagramsJobService regenerateDiagramsJobService,
        AppUserService appUserService,
        SessionStorage sessionStorage,
        ProjectDomainMirrorService projectDomainMirrorService,
        ProjectRepository repository) {

        this.diagramsLocation = diagramsLocation;
        this.diagramService = diagramService;
        this.diagramTypeNoteService = diagramTypeNoteService;
        this.regenerateDiagramsJobService = regenerateDiagramsJobService;
        this.appUserService = appUserService;
        this.sessionStorage = sessionStorage;
        this.projectDomainMirrorService = projectDomainMirrorService;
        this.repository = repository;
    }

    @Override
    public List<Project> getAllAssignedSortedByCreationDate(AppUser appUser) {
        return repository.findByAssignedUsersContainingOrderByCreatedAtAsc(appUser);
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Project getByName(final String projectName) {
        return repository.findByName(buildCleanProjectName(projectName))
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No project found with name: '%s'",
                projectName)));
    }

    @Override
    public Project updateDomainMirror(Project project, Set<String> domainModelPackages, AppUser appUser, Path pathToFile, UploadFileType uploadFileType) {
        checkIsProjectCreator(project, appUser);
        project.setDomainModelPackages(packagesOf(domainModelPackages));
        repository.save(project);
        sessionStorage.createOrUpdate(project, domainModelPackages, pathToFile, uploadFileType);
        return project;
    }

    @Override
    public Project create(String projectName, Set<String> domainModelPackages, AppUser appUser, Path pathToFile, UploadFileType uploadFileType) {
        Project project = mapProject(projectName, appUser);
        project.setDomainModelPackages(packagesOf(domainModelPackages));
        final Project mappedProject = saveWithNameExistsCheck(project);
        sessionStorage.createOrUpdate(mappedProject, domainModelPackages, pathToFile, uploadFileType);
        return mappedProject;
    }

    @Override
    public void createOrUpdateDomainModel(String projectName, byte[] domainMirrorGz, byte[] domainCallsGz,
                                          Collection<String> domainModelPackages) {

        Optional<Project> foundProject = repository.findByName(buildCleanProjectName(projectName));

        if (foundProject.isPresent()) {
            Project project = foundProject.get();
            AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!project.getAssignedUsers().contains(appUser)) {
                throw DiagramViewerException.fail(String.format("User has no access to project '%s'", project.getName()));
            }

            project.setChangedAt(Instant.now());
            project.setDomainModelPackages(packagesOf(domainModelPackages));
            repository.save(project);
            projectDomainMirrorService.createOrUpdateCompressed(project, domainMirrorGz, domainCallsGz);
            return;
        }

        Project project = mapProject(projectName,
            (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        project.setDomainModelPackages(packagesOf(domainModelPackages));
        Project persistedProject = repository.save(project);
        projectDomainMirrorService.createOrUpdateCompressed(persistedProject, domainMirrorGz, domainCallsGz);
    }

    @Override
    public Project rename(Project project, AppUser appUser, String newName) {
        checkIsProjectCreator(project, appUser);
        project.setName(buildCleanProjectName(newName));
        return saveWithNameExistsCheck(project);
    }

    private Project saveWithNameExistsCheck(Project project) {
        String projectName = project.getName();
        Optional<Project> fetchedProject = repository.findByName(projectName);

        if (fetchedProject.isPresent()) {
            throw DiagramViewerException.fail(
                String.format("Project with name '%s' already exists. Please choose a different filename.",
                    projectName));
        }

        return repository.save(project);
    }

    @Override
    public void assignUser(Project project, String emailAddress) {
        Optional<AppUser> foundAppUser = appUserService.find(emailAddress);
        AppUser appUser = foundAppUser.orElseGet(() -> appUserService.createInvitedUser(emailAddress));

        if (userIsAlreadyAssignedToProject(project, appUser)) {
            throw DiagramViewerException.fail(
                String.format("User '%s' is already assigned to project.", appUser.getEmailAddress()));
        }

        project.assignUser(appUser);
        repository.save(project);
    }

    @Override
    public void unassignUser(Project project, AppUser user) {
        if (isProjectCreator(project, user)) return;

        project.unassignUser(user);
        repository.save(project);
    }

    @Override
    public void delete(Project project) {
        final String projectName = project.getName();
        Project fetchedProject = getByName(projectName);
        fetchedProject.unassignAllUsers();

        project.getDiagrams().forEach(diagramTypeNoteService::delete);

        repository.delete(fetchedProject);
        sessionStorage.delete(fetchedProject.getId());

        diagramService.deleteFilesFromFilesystem(project.getId().toString());
    }

    @Override
    public void deleteDiagram(Project project, Diagram diagram) {
        diagramTypeNoteService.delete(diagram);
        regenerateDiagramsJobService.delete(diagram);
        project.removeDiagram(diagram);
        repository.save(project);

        Path diagramPath = Path.of(diagramsLocation, project.getId().toString(), DiagramFileUtils.imageFileName(diagram));

        try {
            FileIOUtils.deleteFileByAbsolutePath(diagramPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw DiagramViewerException.fail(
                "Couldn't finalize deleting diagram because some files couldn't be deleted from the filesystem.", e);
        }
    }

    @Override
    public void deleteDiagramDirectory(Project project, DiagramDirectory diagramDirectory) {
        // nested directories go with their parent; the diagrams of all of them move to the project itself
        List<DiagramDirectory> directories = new ArrayList<>();
        collectWithSubDirectories(project, diagramDirectory, directories);
        directories.forEach(directory -> {
            directory.removeAllDiagrams();
            directory.setParent(null);
            project.removeDiagramDirectory(directory);
        });
        repository.save(project);
    }

    private static void collectWithSubDirectories(Project project, DiagramDirectory directory, List<DiagramDirectory> collected) {
        collected.add(directory);
        project.getSubDirectories(directory).forEach(subDirectory -> collectWithSubDirectories(project, subDirectory, collected));
    }

    private static Set<String> packagesOf(Collection<String> domainModelPackages) {
        return domainModelPackages == null ? new HashSet<>() : new HashSet<>(domainModelPackages);
    }

    private Project mapProject(String projectName, AppUser appUser) {
        return Project.builder()
            .name(buildCleanProjectName(projectName))
            .diagrams(new HashSet<>())
            .diagramDirectories(new HashSet<>())
            .creator(appUser)
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .build();
    }

    private void checkIsProjectCreator(Project project, AppUser appUser) {
        if (!isProjectCreator(project, appUser)) {
            throw DiagramViewerException.fail(String.format("User '%s' is not allowed to update the domain mirror of project '%s'. Only project admins are.", appUser.getEmailAddress(), project.getName()));
        }
    }

    private boolean isProjectCreator(Project project, AppUser user) {
        return Objects.equals(project.getCreator().getId(), user.getId());
    }

    private boolean userIsAlreadyAssignedToProject(Project project, AppUser user) {
        return project.getAssignedUsers().stream().anyMatch(
            appUser -> Objects.equals(user.getId(), appUser.getId()));
    }

    private String buildCleanProjectName(final String projectName) {
        if(projectName == null || projectName.isBlank()) {
            throw DiagramViewerException.fail("Project name may not be empty.");
        }
        return projectName.replaceAll("[.-]", "_");
    }
}
