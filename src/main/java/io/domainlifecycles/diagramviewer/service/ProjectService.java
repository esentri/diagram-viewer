package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface ProjectService {

    Stream<Project> getAll(Path targetDirectory, AuthenticatedUser authenticatedUser);

    Project getByProjectNameClean(final String projectNameClean);

    Project save(Project project);
    Project save(Project project, Diagram diagramToAdd);

    void save(String targetsLocation, InputStream fileContents, String fileName, String boundedContextPackages);
}