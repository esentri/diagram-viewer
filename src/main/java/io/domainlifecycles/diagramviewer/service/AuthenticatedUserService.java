package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;

public interface AuthenticatedUserService {

    boolean userKnown(final String userEmailAddress);
    AuthenticatedUser get(final String userEmailAddress);
    AuthenticatedUser acknowledgeUserAuthentication(String userEmailAddress, String fullName);
    AuthenticatedUser addProject(AuthenticatedUser authenticatedUser, Project project);
    AuthenticatedUser removeProject(AuthenticatedUser authenticatedUser, Project project);
    boolean checkAccess(String projectNameClean, AuthenticatedUser authenticatedUser);
}
