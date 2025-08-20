package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.InvitedUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectFileType;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.model.viewer.User;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.rest.api.model.DomainMirrorUploadDto;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
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

    private final String targetsDirectory;
    private final String diagramsLocation;
    private final DiagramService diagramService;
    private final DiagramTypeNoteService diagramTypeNoteService;
    private final RegenerateDiagramsJobService regenerateDiagramsJobService;
    private final RegisteredUserService registeredUserService;
    private final InvitedUserService invitedUserService;
    private final SessionStorage sessionStorage;
    private final ProjectRepository repository;

    public ProjectServiceImpl(
        @Value("${targets.location}") String targetsDirectory,
        @Value("${diagrams.location}") String diagramsLocation,
        DiagramService diagramService,
        DiagramTypeNoteService diagramTypeNoteService, RegenerateDiagramsJobService regenerateDiagramsJobService, RegisteredUserService registeredUserService,
        InvitedUserService invitedUserService,
        SessionStorage sessionStorage,
        ProjectRepository repository) {

        this.targetsDirectory = targetsDirectory;
        this.diagramsLocation = diagramsLocation;
        this.diagramService = diagramService;
        this.diagramTypeNoteService = diagramTypeNoteService;
        this.regenerateDiagramsJobService = regenerateDiagramsJobService;
        this.registeredUserService = registeredUserService;
        this.invitedUserService = invitedUserService;
        this.sessionStorage = sessionStorage;
        this.repository = repository;
    }

    @Override
    public Stream<Project> getAll(RegisteredUser registeredUser) {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
            .filter(project -> project.getAssignedRegisteredUsers().stream()
                .anyMatch(assignedUser -> Objects.equals(assignedUser.getId(), registeredUser.getId())));
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Project getByName(final String projectName) {
        return repository.findByName(buildCleanProjectName(projectName))
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No project found with name: %s",
                projectName)));
    }

    @Override
    public void update(Project project,
                       String projectName,
                       Set<String> domainModelPackages) {

        project.setName(buildCleanProjectName(projectName));
        checkProjectValueRequirements(project);
        Path projectFilePath = buildProjectFilePath(project);

        if(Objects.equals(project.getName(), projectName)) {
            Project persistedProject = repository.save(project);
            sessionStorage.createOrUpdate(persistedProject, projectFilePath, domainModelPackages);
            return;
        }

        Project persistedProject = insert(project);
        sessionStorage.createOrUpdate(persistedProject, projectFilePath, domainModelPackages);
    }

    @Override
    public void deleteDiagram(Project project, Diagram diagram) {
        diagramTypeNoteService.delete(diagram);
        regenerateDiagramsJobService.delete(diagram);
        project.removeDiagram(diagram);
        repository.save(project);

        Path diagramPath = Path.of(diagramsLocation, project.getId().toString(),
            diagram.getFileName());

        try {
            FileIOUtils.deleteFileByAbsolutePath(diagramPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw DiagramViewerException.fail("Couldn't finalize deleting diagram because some files couldn't be deleted from the filesystem.", e);
        }
    }

    @Override
    public void deleteDiagramDirectory(Project project, DiagramDirectory diagramDirectory) {
        project.removeDiagramDirectory(diagramDirectory);
        diagramDirectory.removeAllDiagrams();
        repository.save(project);
    }

    @Override
    public Project create(
        String projectName, RegisteredUser registeredUser,
        InputStream jarFile,
        String jarFileName,
        Set<String> domainModelPackages) {

        final Project mappedProject = insert(mapProject(projectName, jarFileName, domainModelPackages, registeredUser, false));
        return saveProjectFileAndCreateProjectAndDomainMirror(jarFile, domainModelPackages, mappedProject);
    }

    @Override
    public void updateJarFile(
            Project project,
            InputStream jarFile,
            String filename,
            Set<String> domainModelPackages) {

        deleteTargetFile(project);
        saveProjectFileAndCreateProjectAndDomainMirror(jarFile, domainModelPackages, project);
    }

    @Override
    public void createOrUpdateDomainMirror(
            String projectName,
            String fileName,
            DomainMirrorUploadDto domainMirrorUploadDto) {

        Optional<Project> foundProject = repository.findByName(buildCleanProjectName(projectName));

        if(foundProject.isPresent()) {
            Project project = foundProject.get();
            var registeredUser = (RegisteredUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!project.getAssignedRegisteredUsers().contains(registeredUser)){
                throw new IllegalStateException(String.format("User has no access to project '%s'",project.getName()));
            }

            if(!project.isApiUpload()) {
                deleteTargetFile(project);
            }

            project.setChangedAt(Instant.now());
            repository.save(project);
            sessionStorage.createOrUpdate(project, domainMirrorUploadDto.domainMirror());
            return;
        }

        Project project = mapProject(projectName, fileName,
            domainMirrorUploadDto.domainModelPackages(),
            (RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), true);
        Project persistedProject = repository.save(project);
        sessionStorage.createOrUpdate(persistedProject, domainMirrorUploadDto.domainMirror());
    }

    @Override
    public void assignUser(Project project, String emailAddress) {
        if(project.getAssignedRegisteredUsers().stream()
            .anyMatch(user -> Objects.equals(user.getEmailAddress(), emailAddress))) return;

        boolean userIsSignedUp = registeredUserService.userKnown(emailAddress);
        final User user = userIsSignedUp ? registeredUserService.get(emailAddress) : invitedUserService.getOrCreate(emailAddress);

        assignUser(project, user);
    }

    @Override
    public void assignUser(Project project, User user) {
        if(userIsAlreadyAssignedToProject(project, user)) {
            throw DiagramViewerException.fail(String.format("User '%s' is already assigned to project.", user.getEmailAddress()));
        }

        project.assignUser(user);
        repository.save(project);
    }

    @Override
    public void unassignUser(Project project, User user) {
        if(user instanceof RegisteredUser && Objects.equals(project.getCreator().getId(), ((RegisteredUser) user).getId())) return;

        project.unassignUser(user);
        repository.save(project);

        if(user instanceof InvitedUser && invitedUserService.checkForRemoval((InvitedUser) user))
            invitedUserService.delete((InvitedUser) user);
    }

    @Override
    public void delete(Project project) {
        final String projectName = project.getName();
        Project fetchedProject = getByName(projectName);
        fetchedProject.unassignAllUsers();

        project.getDiagrams().forEach(diagramTypeNoteService::delete);

        repository.delete(fetchedProject);
        sessionStorage.delete(fetchedProject.getId());

        if(!project.isApiUpload()) {
            deleteTargetFile(project);
        }

        diagramService.deleteFilesFromFilesystem(project.getId().toString());
    }

    private Project insert(Project project) {
        String projectName = project.getName();
        Optional<Project> fetchedProject = repository.findByName(projectName);

        if(fetchedProject.isPresent()) {
            throw DiagramViewerException.fail(String.format("Project with name '%s' already exists. Please choose a different filename.",
                projectName));
        }

        return repository.save(project);
    }

    private Project mapProject(String projectName, String fileName, Set<String> domainModelPackages, RegisteredUser registeredUser, boolean apiUpload) {
        return Project.builder()
            .name(buildCleanProjectName(projectName))
            .diagrams(new HashSet<>())
            .diagramDirectories(new HashSet<>())
            .apiUpload(apiUpload)
            .projectFileType(!apiUpload ? getFileType(fileName) : null)
            .creator(registeredUser)
            .domainModelPackages(domainModelPackages)
            .assignedRegisteredUsers(new HashSet<>(Set.of(registeredUser)))
            .assignedInvitedUsers(new HashSet<>())
            .build();
    }

    private Project saveProjectFileAndCreateProjectAndDomainMirror(InputStream fileContents, Set<String> domainModelPackages, Project project) {
        Path projectFilePath = saveTargetFile(targetsDirectory, fileContents,
            buildProjectFilename(project));

        try {
            sessionStorage.createOrUpdate(project, projectFilePath, domainModelPackages);
            return repository.save(project);
        } catch(RuntimeException e) {
            deleteTargetFile(project);
            throw e;
        }
    }

    private Path saveTargetFile(String targetsLocation, InputStream fileContents, String fileName) {
        try {
            Path filePath = FileIOUtils.saveFile(targetsLocation, fileName, fileContents);
            LOGGER.info(String.format("Successfully uploaded file '%s' to '%s'.", fileName, targetsLocation));
            return filePath;
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Couldn't save file '%s' to '%s'.", fileName,
                targetsLocation), e);
        }
    }

    private void deleteTargetFile(Project project) {
        try {
            String absolutePathToTarget = Path.of(targetsDirectory, buildProjectFilename(project)).toAbsolutePath().toString();
            FileIOUtils.deleteFileByAbsolutePath(absolutePathToTarget);
        } catch (IOException e) {
            throw DiagramViewerException.fail("Couldn't finalize deleting project because some files couldn't be deleted from the filesystem.", e);
        }
    }

    private Path buildProjectFilePath(Project project) {
        return Path.of(targetsDirectory, buildProjectFilename(project));
    }

    private String buildProjectFilename(Project project) {
        return project.getId() + project.getProjectFileType().getFileTypeEnding();
    }

    private boolean userIsAlreadyAssignedToProject(Project project, User user) {
        return user instanceof InvitedUser && project.getAssignedInvitedUsers().stream().anyMatch(
            invitedUser -> Objects.equals(((InvitedUser) user).getId(), invitedUser.getId()))
            || user instanceof RegisteredUser && project.getAssignedRegisteredUsers().stream().anyMatch(
            registeredUser -> Objects.equals(
                ((RegisteredUser) user).getId(), registeredUser.getId()));
    }


    private void checkProjectValueRequirements(Project project) {
        if(project.getName() == null || project.getName().isBlank()) {
            throw DiagramViewerException.fail("Project name may not be empty.");
        }
    }

    private String buildCleanProjectName(final String projectName) {
        return projectName == null || projectName.isBlank() ? projectName : projectName.replaceAll("[.-]", "_");
    }

    private ProjectFileType getFileType(String fileName) {
        if(fileName.endsWith(".jar")) {
            return ProjectFileType.JAR;
        } else if(fileName.endsWith(".json")) {
            return ProjectFileType.JSON;
        }
        throw DiagramViewerException.fail(String.format("Uploaded project file '%s' has an unsupported file extension.", fileName));
    }
}
