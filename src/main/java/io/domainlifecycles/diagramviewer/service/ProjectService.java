package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.User;
import java.io.InputStream;
import java.util.stream.Stream;

public interface ProjectService {

    Stream<Project> getAll(AuthenticatedUser authenticatedUser);

    Project getByName(final String projectName);

    void update(Project project);

    void deleteDiagram(Project project, Diagram diagram);

    Project save(AuthenticatedUser authenticatedUser, InputStream fileContents,
                 String fileName, String boundedContextPackages);

    void updateTargetFile(Project project, InputStream fileContents, String filename, String boundedContextPackages);

    void assignUser(Project project, String emailAddress);

    void assignUser(Project project, User user);

    void unassignUser(Project project, User user);

    void delete(Project project);
}