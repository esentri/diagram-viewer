package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;

public interface TemporaryUserService {

    boolean userKnown(final String userEmailAddress);
    TemporaryUser getOrCreate(String userEmailAddress);
    TemporaryUser get(String userEmailAddress);
    TemporaryUser addProject(TemporaryUser temporaryUser, Project project);
    TemporaryUser removeProject(TemporaryUser temporaryUser, Project project);
    void delete(TemporaryUser temporaryUser);
}
