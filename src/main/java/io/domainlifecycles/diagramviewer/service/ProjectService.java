package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Project;
import java.util.List;
import java.util.stream.Stream;

public interface ProjectService {

    Stream<Project> getAll();
    Project get(final String targetName);
}