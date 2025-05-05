package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.RegisteredUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;

public interface ProjectService {

    Stream<Project> getAll(RegisteredUser registeredUser);

    Project getByName(final String projectName);

    void update(Project project, String projectName, Set<String> domainModelPackages, Set<String> boundedContextPackages);

    void deleteDiagram(Project project, Diagram diagram);

    Project save(RegisteredUser registeredUser,
                 InputStream fileContents,
                 String fileName,
                 Set<String> domainModelPackages,
                 Set<String> boundedContextPackages);

    void updateTargetFile(Project project, InputStream fileContents, String filename, Set<String> domainModelPackages, Set<String> boundedContextPackages);

    void createOrUpdateDomainMirror(String projectName, DomainMirror domainMirror);

    void assignUser(Project project, String emailAddress);

    void assignUser(Project project, User user);

    void unassignUser(Project project, User user);

    void delete(Project project);
}