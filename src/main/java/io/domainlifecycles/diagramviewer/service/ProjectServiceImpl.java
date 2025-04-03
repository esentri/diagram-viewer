package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
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


    private final DomainModelSessionStorage sessionStorage;

    private final UserService userService;
    private final ProjectRepository repository;

    private String targetsDirectory;


    public ProjectServiceImpl(
        @Value("${targets.location}") String defaultTargetsDirectory,
        DomainModelSessionStorage sessionStorage,
        UserService userService,
        ProjectRepository repository) {

        this.targetsDirectory = defaultTargetsDirectory;
        this.sessionStorage = sessionStorage;
        this.userService = userService;
        this.repository = repository;
        initializeAllDomainModels();
    }

    @Override
    public Stream<Project> getAll(Path targetDirectory, User user) {
        return getAll(targetDirectory)
            .filter(project -> project.getAssignedUsers().stream()
                .anyMatch(assignedUser -> Objects.equals(assignedUser.getUserId(), user.getUserId())));
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
        userService.addProject(sessionStorage.getAuthenticatedUser(), persistedProject);
        return persistedProject;
    }

    @Override
    public Project save(Project project, Diagram diagramToAdd) {
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

    private Project mapProject(String targetsLocation, String fileName, String boundedContextPackages) {
        Path filePath = Path.of(targetsLocation);
        List<String> boundedContexts = Arrays.stream(boundedContextPackages.split(",")).toList();
        User authenticatedUser = sessionStorage.getAuthenticatedUser();

        return Project.builder()
            .projectNameFull(fileName)
            .projectNameClean(buildCleanFileName(fileName))
            .absolutePathToTarget(filePath.toAbsolutePath() + "/" + fileName)
            .boundedContextPackages(boundedContexts)
            .creator(authenticatedUser)
            .assignedUsers(List.of(authenticatedUser))
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
