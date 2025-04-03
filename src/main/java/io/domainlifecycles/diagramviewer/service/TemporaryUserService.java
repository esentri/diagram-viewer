package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;

public interface TemporaryUserService {

    boolean userKnown(final String userEmailAddress);
    void add(String userEmailAddress, String fullName);
    void addToProject(String userEmailAddress, Project project);
    TemporaryUser get(String userEmailAddress);

    void delete(TemporaryUser temporaryUser);
}
