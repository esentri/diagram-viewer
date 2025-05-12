package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.InvitedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.RegisteredUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.rest.api.model.DomainMirrorUploadDto;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private final RegisteredUserService registeredUserService;
    private final InvitedUserService invitedUserService;
    private final SessionStorage sessionStorage;
    private final ProjectRepository repository;

    public ProjectServiceImpl(
        @Value("${targets.location}") String targetsDirectory,
        @Value("${diagrams.location}") String diagramsLocation,
        DiagramService diagramService,
        RegisteredUserService registeredUserService,
        InvitedUserService invitedUserService,
        SessionStorage sessionStorage,
        ProjectRepository repository) {

        this.targetsDirectory = targetsDirectory;
        this.diagramsLocation = diagramsLocation;
        this.diagramService = diagramService;
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
    public Project getByName(final String projectName) {
        return repository.findByName(projectName)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No project found with name: %s",
                projectName)));
    }

    @Override
    public void update(Project project,
                       String projectName,
                       Set<String> domainModelPackages) {

        project.setName(buildCleanFileName(projectName));
        checkProjectValueRequirements(project);
        Path projectFilePath = buildProjectFilePath(project);

        if(Objects.equals(project.getName(), projectName)) {
            Project persistedProject = repository.save(project);
            sessionStorage.createOrUpdate(persistedProject.getId(), projectFilePath, domainModelPackages);
            return;
        }

        Project persistedProject = insert(project);
        sessionStorage.createOrUpdate(persistedProject.getId(), projectFilePath, domainModelPackages);
    }

    @Override
    public void deleteDiagram(Project project, Diagram diagram) {
        project.getDiagrams().remove(diagram);
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
    public Project save(
            RegisteredUser registeredUser,
            InputStream fileContents,
            String fileName,
            Set<String> domainModelPackages) {

        final Project mappedProject = insert(mapProject(fileName, domainModelPackages, registeredUser, false));
        return saveProjectFileAndCreateProjectAndDomainMirror(fileContents, domainModelPackages, mappedProject);
    }

    @Override
    public void updateTargetFile(
            Project project,
            InputStream fileContents,
            String filename,
            Set<String> domainModelPackages) {

        deleteTargetFile(project);
        saveProjectFileAndCreateProjectAndDomainMirror(fileContents, domainModelPackages, project);
    }

    @Override
    public void createOrUpdateDomainMirror(
            String projectName,
            DomainMirrorUploadDto domainMirrorUploadDto) {

        Optional<Project> foundProject = repository.findByName(projectName);

        if(foundProject.isPresent()) {
            Project project = foundProject.get();

            if(!project.isApiUpload()) {
                deleteTargetFile(project);
            }

            sessionStorage.createOrUpdate(project.getId(), domainMirrorUploadDto.domainMirror());
            return;
        }

        Project project = mapProject(projectName,
            domainMirrorUploadDto.domainModelPackages(),
            (RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), true);
        Project persistedProject = repository.save(project);
        sessionStorage.createOrUpdate(persistedProject.getId(), domainMirrorUploadDto.domainMirror());
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

    private Project mapProject(String fileName, Set<String> domainModelPackages, RegisteredUser registeredUser, boolean apiUpload) {
        return Project.builder()
            .name(buildCleanFileName(fileName))
            .diagrams(new HashSet<>())
            .diagramDirectories(new HashSet<>())
            .apiUpload(apiUpload)
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
            sessionStorage.createOrUpdate(project.getId(), projectFilePath, domainModelPackages);
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
        return project.getId() + ".jar";
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

    private String buildCleanFileName(final String fileName) {
        return fileName == null || fileName.isBlank() ? fileName : fileName.replaceAll("[.-]", "_");
    }
}
