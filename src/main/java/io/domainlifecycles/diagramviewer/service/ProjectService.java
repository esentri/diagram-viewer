package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.model.viewer.User;
import io.domainlifecycles.diagramviewer.rest.api.model.DomainMirrorUploadDto;
import java.io.InputStream;
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

    @Transactional
    Project create(String projectName, RegisteredUser registeredUser,
                   InputStream jarFile,
                   String jarFileName,
                   Set<String> domainModelPackages);

    @Transactional
    void update(Project project, String projectName, Set<String> domainModelPackages);

    @Transactional
    void updateJarFile(Project project, InputStream jarFile, String filename, Set<String> domainModelPackages);

    @Transactional
    void createOrUpdateDomainMirror(String projectName, String fileName, DomainMirrorUploadDto domainMirrorUploadDto);

    @Transactional
    void delete(Project project);

    void deleteDiagram(Project project, Diagram diagram);

    void deleteDiagramDirectory(Project project, DiagramDirectory diagramDirectory);

    void assignUser(Project project, String emailAddress);

    void assignUser(Project project, User user);

    void unassignUser(Project project, User user);
}