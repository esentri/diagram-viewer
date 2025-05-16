package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;

public interface SecurityService {

    RegisteredUser getCurrentlySignedInUser();

    RegisteredUser acknowledgeUserAuthentication(String userEmailAddress, String fullName);

    boolean checkAccess(String projectName, RegisteredUser registeredUser);
}
