package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;

public interface SecurityService {

    AuthenticatedUser getAuthenticatedUser();

    AuthenticatedUser acknowledgeUserAuthentication(String userEmailAddress, String fullName);

    boolean checkAccess(String projectName, AuthenticatedUser authenticatedUser);
}
