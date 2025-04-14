package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.User;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface ProjectService {

    Stream<Project> getAll(Path targetDirectory, AuthenticatedUser authenticatedUser);

    Project getByProjectNameClean(final String projectNameClean);

    Project update(Project project);

    Project deleteDiagram(Project project, Diagram diagram);

    Project save(AuthenticatedUser authenticatedUser, String targetsLocation, InputStream fileContents,
                 String fileName, String boundedContextPackages);

    Project assignUser(Project project, String emailAddress);

    Project assignUser(Project project, User user);

    Project unassignUser(Project project, User user);

    void delete(Project project);
}