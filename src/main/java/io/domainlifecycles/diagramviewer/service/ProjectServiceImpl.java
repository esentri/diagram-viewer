package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.InvitedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.RegisteredUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.mirror.api.DomainModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final String targetsDirectory;
    private final String diagramsLocation;
    private final DiagramService diagramService;
    private final RegisteredUserService registeredUserService;
    private final InvitedUserService invitedUserService;
    private final ProjectRepository repository;

    public ProjectServiceImpl(
        @Value("${targets.location}") String targetsDirectory,
        @Value("${diagrams.location}") String diagramsLocation,
        DiagramService diagramService,
        RegisteredUserService registeredUserService,
        InvitedUserService invitedUserService,
        ProjectRepository repository) {

        this.targetsDirectory = targetsDirectory;
        this.diagramsLocation = diagramsLocation;
        this.diagramService = diagramService;
        this.registeredUserService = registeredUserService;
        this.invitedUserService = invitedUserService;
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
    public void update(Project project) {
        project.setName(buildCleanFileName(project.getName()));
        checkProjectValueRequirements(project);
        insert(project);
    }

    @Override
    public void deleteDiagram(Project project, Diagram diagram) {
        Path diagramPath = Path.of(diagramsLocation, project.getId().toString(),
            diagram.getFileName());

        project.getDiagrams().remove(diagram);
        repository.save(project);

        try {
            FileIOUtils.deleteFileByAbsolutePath(diagramPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw DiagramViewerException.fail("Couldn't finalize deleting diagram because some files couldn't be deleted from the filesystem.", e);
        }
    }

    @Override
    public Project save(RegisteredUser registeredUser, InputStream fileContents, String fileName, String boundedContextPackages) {

        // persist project without domain model to obtain UUID
        String[] boundedContexts = boundedContextPackages.split(",");
        final Project mappedProject = insert(mapProject(fileName, boundedContexts, registeredUser));

        Path projectFilePath = saveTargetFile(targetsDirectory, fileContents,
            buildProjectFilename(mappedProject));

        DomainModel domainModel = DomainModelUtils.initializeDomainModelFromJar(projectFilePath,
            boundedContexts);
        mappedProject.setDomainModel(domainModel);

        return repository.save(mappedProject);
    }

    @Override
    public void updateTargetFile(Project project, InputStream fileContents, String filename, String boundedContextPackages) {
        deleteTargetFile(project);

        String[] boundedContexts = boundedContextPackages.split(",");
        Path projectFilePath = saveTargetFile(targetsDirectory, fileContents, buildProjectFilename(project));
        DomainModel domainModel = DomainModelUtils.initializeDomainModelFromJar(projectFilePath,
            boundedContexts);

        project.setDomainModel(domainModel);

        repository.save(project);
    }

    @Override
    public void createOrUpdateDomainModel(String projectName, DomainModel domainModel) {
        Optional<Project> foundProject = repository.findByName(projectName);

        if(foundProject.isPresent()) {
            Project project = foundProject.get();
            project.setDomainModel(domainModel);
            repository.save(project);
            return;
        }

        Project project = mapProject(projectName, domainModel,
            (RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        repository.save(project);
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
        update(project);
    }

    @Override
    public void unassignUser(Project project, User user) {
        if(user instanceof RegisteredUser && Objects.equals(project.getCreator().getId(), ((RegisteredUser) user).getId())) return;

        project.unassignUser(user);
        update(project);

        if(user instanceof InvitedUser && invitedUserService.checkForRemoval((InvitedUser) user))
            invitedUserService.delete((InvitedUser) user);
    }

    @Override
    public void delete(Project project) {
        final String projectName = project.getName();
        Project fetchedProject = getByName(projectName);
        fetchedProject.unassignAllUsers();
        repository.delete(fetchedProject);

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

    private Project mapProject(String fileName, String[] boundedContextPackages, RegisteredUser registeredUser) {
        final List<String> boundedContexts = Arrays.stream(boundedContextPackages).toList();

        return Project.builder()
            .name(buildCleanFileName(fileName))
            .boundedContextPackages(boundedContexts)
            .apiUpload(false)
            .creator(registeredUser)
            .assignedRegisteredUsers(new ArrayList<>(List.of(registeredUser)))
            .build();
    }

    private Project mapProject(String projectName, DomainModel domainModel, RegisteredUser registeredUser) {
        return Project.builder()
            .name(projectName)
            .domainModel(domainModel)
            .apiUpload(true)
            .creator(registeredUser)
            .assignedRegisteredUsers(new ArrayList<>(List.of(registeredUser)))
            .build();
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

        if(project.getBoundedContextPackages().isEmpty() || project.getBoundedContextPackages().get(0).isBlank()) {
            throw DiagramViewerException.fail("Project has to have at least one bounded context package.");
        }
    }

    private String buildCleanFileName(final String fileName) {
        return fileName == null || fileName.isBlank() ? fileName : fileName.replaceAll("[.-]", "_");
    }
}
