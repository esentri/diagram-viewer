package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final DiagramService diagramService;
    private final AuthenticatedUserService authenticatedUserService;
    private final TemporaryUserService temporaryUserService;
    private final ProjectRepository repository;

    public ProjectServiceImpl(
        DiagramService diagramService,
        AuthenticatedUserService authenticatedUserService,
        TemporaryUserService temporaryUserService,
        ProjectRepository repository) {

        this.diagramService = diagramService;
        this.authenticatedUserService = authenticatedUserService;
        this.temporaryUserService = temporaryUserService;
        this.repository = repository;
    }

    @Override
    public Stream<Project> getAll(Path targetDirectory, AuthenticatedUser authenticatedUser) {
        return getAll(targetDirectory)
            .filter(project -> project.getAssignedAuthenticatedUsers().stream()
                .anyMatch(assignedUser -> Objects.equals(assignedUser.getId(), authenticatedUser.getId())));
    }

    private Stream<Project> getAll(Path targetDirectory) {
        Set<File> allFilesInDirectory = FileIOUtils.getFilesInDirectory(targetDirectory);

        return allFilesInDirectory.stream()
            .map(targetFile -> {
                Optional<Project> project = repository.findByAbsolutePathToTarget(targetFile.getAbsolutePath());
                return project.orElse(null);
            })
            .filter(Objects::nonNull);
    }

    @Override
    public Project getByProjectNameClean(final String projectNameClean) {
        return repository.findByProjectNameClean(projectNameClean)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No project found with name: %s",
                projectNameClean)));
    }

    @Override
    public Project update(Project project) {

        // Ensure Project Name Clean still meets the requirements
        project.setDisplayName(buildCleanFileName(project.getDisplayName()));
        checkProjectValueRequirements(project);

        return repository.save(project);
    }

    @Override
    public Project removeDiagram(Project project, Diagram diagram) {
        final String diagramPath = diagram.getFullAbsoluteLocationPath();

        project.getDiagrams().remove(diagram);
        Project persistedProject = repository.save(project);

        try {
            FileIOUtils.deleteFileByAbsolutePath(diagramPath);
            return persistedProject;
        } catch (IOException e) {
            throw DiagramViewerException.fail("Couldn't finalize deleting diagram because some files couldn't be deleted from the filesystem.", e);
        }
    }

    @Override
    public Project save(AuthenticatedUser authenticatedUser, String targetsLocation, InputStream fileContents, String fileName, String boundedContextPackages) {
        final Project project = mapNewProject(targetsLocation, fileName, boundedContextPackages, authenticatedUser);
        checkProjectValueRequirements(project);
        Project persistedProject = create(project);

        try {
            FileIOUtils.saveFile(targetsLocation, fileName, fileContents);
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Couldn't save file '%s' to '%s'.", fileName,
                targetsLocation), e);
        }

        return persistedProject;
    }

    @Override
    public void assignUser(Project project, String emailAddress) {
        if(project.getAssignedAuthenticatedUsers().stream()
            .anyMatch(user -> Objects.equals(user.getEmailAddress(), emailAddress))) return;

        boolean userIsSignedUp = authenticatedUserService.userKnown(emailAddress);
        final User user = userIsSignedUp ? authenticatedUserService.get(emailAddress) : temporaryUserService.getOrCreate(emailAddress);

        final Project fetchedProject = repository.findById(project.getId()).get();
        fetchedProject.assignUser(user);
        Project updatedProject = update(fetchedProject);

        if(user instanceof AuthenticatedUser) {
            authenticatedUserService.addProject((AuthenticatedUser) user, updatedProject);
        } else {
            temporaryUserService.addProject((TemporaryUser) user, updatedProject);
        }
    }

    @Override
    public void unassignUser(Project project, User user) {
        if(user instanceof AuthenticatedUser && Objects.equals(project.getCreator().getId(), ((AuthenticatedUser) user).getId())) return;

        final Project fetchedProject = repository.findById(project.getId()).get();
        fetchedProject.unassignUser(user);
        Project updatedProject = update(fetchedProject);

        if(user instanceof AuthenticatedUser) {
            authenticatedUserService.removeProject((AuthenticatedUser) user, updatedProject);
        } else {
            TemporaryUser updatedUser = temporaryUserService.removeProject((TemporaryUser) user, updatedProject);
            if(updatedUser.getAssignedProjects().isEmpty()) temporaryUserService.delete(updatedUser);
        }
    }

    @Override
    public void delete(Project project) {
        String projectNameClean = project.getProjectNameClean();
        Project fetchedProject = getByProjectNameClean(projectNameClean);
        String absolutePathToTarget = fetchedProject.getAbsolutePathToTarget();

        fetchedProject.getAssignedTemporaryUsers().clear();
        fetchedProject.getAssignedAuthenticatedUsers().clear();
        repository.delete(fetchedProject);

        try {
            FileIOUtils.deleteFileByAbsolutePath(absolutePathToTarget);
            diagramService.deleteFilesFromFilesystem(projectNameClean);
        } catch (IOException e) {
            throw DiagramViewerException.fail("Couldn't finalize deleting project because some files couldn't be deleted from the filesystem.", e);
        }
    }

    private Project create(Project project) {
        String projectNameClean = project.getProjectNameClean();
        Optional<Project> fetchedProject = repository.findByProjectNameClean(projectNameClean);

        if(fetchedProject.isPresent()) {
            throw DiagramViewerException.fail(String.format("Project with name '%s' already exists. Please choose a different filename.",
                projectNameClean));
        }

        return repository.save(project);
    }

    private Project mapNewProject(String targetsLocation, String fileName, String boundedContextPackages, AuthenticatedUser authenticatedUser) {
        final Path filePath = Path.of(targetsLocation);
        final List<String> boundedContexts = Arrays.stream(boundedContextPackages.split(",")).toList();
        final String projectNameClean = buildCleanFileName(fileName);

        return Project.builder()
            .projectNameFull(fileName)
            .projectNameClean(projectNameClean)
            .displayName(projectNameClean)
            .absolutePathToTarget(filePath.toAbsolutePath() + "/" + fileName)
            .boundedContextPackages(boundedContexts)
            .creator(authenticatedUser)
            .assignedAuthenticatedUsers(List.of(authenticatedUser))
            .build();
    }

    private void checkProjectValueRequirements(Project project) {
        if(project.getDisplayName() == null || project.getDisplayName().isBlank()) {
            throw DiagramViewerException.fail("Project name may not be empty.");
        }

        if(project.getBoundedContextPackages().isEmpty() || project.getBoundedContextPackages().get(0).isBlank()) {
            throw DiagramViewerException.fail("Project has to have at least one bounded context package");
        }
    }

    private String buildCleanFileName(final String fileName) {
        return fileName == null || fileName.isBlank() ? fileName : fileName.replaceAll("[.-]", "_");
    }
}
