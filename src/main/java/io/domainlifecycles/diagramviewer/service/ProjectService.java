package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.model.viewer.User;
import io.domainlifecycles.diagramviewer.rest.api.model.DomainMirrorUploadDto;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectService {

    @Transactional
    Stream<Project> getAll(RegisteredUser registeredUser);

    Project getByName(final String projectName);

    @Transactional
    void update(Project project, String projectName, Set<String> domainModelPackages);

    void deleteDiagram(Project project, Diagram diagram);

    void deleteDiagramDirectory(Project project, DiagramDirectory diagramDirectory);

    @Transactional
    Project save(RegisteredUser registeredUser,
                 InputStream fileContents,
                 String fileName,
                 Set<String> domainModelPackages);

    @Transactional
    void updateTargetFile(Project project, InputStream fileContents, String filename, Set<String> domainModelPackages);

    @Transactional
    void createOrUpdateDomainMirror(String projectName, DomainMirrorUploadDto domainMirrorUploadDto);

    @Transactional
    void delete(Project project);

    void assignUser(Project project, String emailAddress);

    void assignUser(Project project, User user);

    void unassignUser(Project project, User user);
}