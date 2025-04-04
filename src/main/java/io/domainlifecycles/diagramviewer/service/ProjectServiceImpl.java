package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.session.SessionStorage;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final SessionStorage sessionStorage;
    private final AuthenticatedUserService authenticatedUserService;
    private final TemporaryUserService temporaryUserService;
    private final ProjectRepository repository;
    private final String targetsDirectory;

    public ProjectServiceImpl(
        @Value("${targets.location}") String defaultTargetsDirectory,
        SessionStorage sessionStorage,
        AuthenticatedUserService authenticatedUserService,
        TemporaryUserService temporaryUserService, ProjectRepository repository) {

        this.targetsDirectory = defaultTargetsDirectory;
        this.sessionStorage = sessionStorage;
        this.authenticatedUserService = authenticatedUserService;
        this.temporaryUserService = temporaryUserService;
        this.repository = repository;
        initializeAllDomainModels();
    }

    @Override
    public Stream<Project> getAll(Path targetDirectory, AuthenticatedUser authenticatedUser) {
        return getAll(targetDirectory)
            .filter(project -> project.getAssignedAuthenticatedUsers().stream()
                .anyMatch(assignedUser -> Objects.equals(assignedUser.getAuthenticatedUserId(), authenticatedUser.getAuthenticatedUserId())));
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
    public Project save(Project project) {
        Project persistedProject = repository.save(project);
        authenticatedUserService.addProject(sessionStorage.getAuthenticatedUser(), persistedProject);
        return persistedProject;
    }

    @Override
    public Project addDiagram(Project project, Diagram diagramToAdd) {
        project.addDiagram(diagramToAdd);
        return repository.save(project);
    }

    @Override
    public void save(String targetsLocation, InputStream fileContents, String fileName, String boundedContextPackages) {
        final Project project = mapProject(targetsLocation, fileName, boundedContextPackages);
        Project persistedProject = save(project);

        try {
            FileIOUtils.saveFile(targetsLocation, fileName, fileContents);
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Couldn't save file '%s' to '%s'.", fileName,
                targetsLocation), e);
        }

        sessionStorage.add(persistedProject);
    }

    @Override
    public void assignUser(Project project, String emailAddress) {
        if(project.getAssignedAuthenticatedUsers().stream()
            .anyMatch(user -> Objects.equals(user.getEmailAddress(), emailAddress))) return;

        boolean userIsSignedUp = authenticatedUserService.userKnown(emailAddress);

        final User user = userIsSignedUp ? authenticatedUserService.get(emailAddress) : temporaryUserService.create(emailAddress);
        project.assignUser(user);
        save(project);
    }

    @Override
    public void unassignUser(Project project, User user) {
        if(user instanceof AuthenticatedUser && Objects.equals(project.getCreator().getAuthenticatedUserId(), ((AuthenticatedUser) user).getAuthenticatedUserId())) return;

        project.unassignUser(user);
        save(project);

        if(user instanceof TemporaryUser && user.getAssignedProjects().isEmpty()) temporaryUserService.delete((TemporaryUser) user);
    }

    private Project mapProject(String targetsLocation, String fileName, String boundedContextPackages) {
        Path filePath = Path.of(targetsLocation);
        List<String> boundedContexts = Arrays.stream(boundedContextPackages.split(",")).toList();
        AuthenticatedUser authenticatedAuthenticatedUser = sessionStorage.getAuthenticatedUser();

        return Project.builder()
            .projectNameFull(fileName)
            .projectNameClean(buildCleanFileName(fileName))
            .absolutePathToTarget(filePath.toAbsolutePath() + "/" + fileName)
            .boundedContextPackages(boundedContexts)
            .creator(authenticatedAuthenticatedUser)
            .assignedAuthenticatedUsers(List.of(authenticatedAuthenticatedUser))
            .build();
    }

    private String buildCleanFileName(final String fileName) {
        return fileName == null || fileName.isBlank() ? fileName : fileName.replaceAll("[.-]", "_");
    }

    private void initializeAllDomainModels(){
        if (targetsDirectory != null) {
            File dir = new File(targetsDirectory);

            if(dir.exists()) {
                getAll(dir.toPath()).forEach(sessionStorage::add);
            }
        }
    }
}
