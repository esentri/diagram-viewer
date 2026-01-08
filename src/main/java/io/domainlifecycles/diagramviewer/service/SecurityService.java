package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.AppUser;

public interface SecurityService {

    AppUser getCurrentlySignedInUser();

    AppUser acknowledgeOktaUserAuthentication(String userEmailAddress, String firstName, String lastName, String sub);

    void registerInternalUser(String email, String firstName, String lastName, String rawPassword);

    boolean checkAccess(String projectName, AppUser appUser);
}
