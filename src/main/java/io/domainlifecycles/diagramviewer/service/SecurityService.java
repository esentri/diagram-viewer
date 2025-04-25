package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.RegisteredUser;

public interface SecurityService {

    RegisteredUser getRegisteredUser();

    RegisteredUser acknowledgeUserAuthentication(String userEmailAddress, String fullName);

    boolean checkAccess(String projectName, RegisteredUser registeredUser);
}
