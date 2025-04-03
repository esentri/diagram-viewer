package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.User;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface ProjectService {

    Stream<Project> getAll(Path targetDirectory, User user);

    Project getByProjectNameClean(final String projectNameClean);

    void save(Project project);

    void save(String targetsLocation, InputStream fileContents, String fileName, String boundedContextPackages);
}