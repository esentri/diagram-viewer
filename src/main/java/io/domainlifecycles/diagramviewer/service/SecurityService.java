package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.AppUser;

public interface SecurityService {

    AppUser getCurrentlySignedInUser();

    AppUser acknowledgeOAuth2UserAuthentication(String userEmailAddress, String firstName, String lastName, String sub);

    void registerSelfServiceUser(String email, String firstName, String lastName, String rawPassword);

    boolean checkAccess(String projectName, AppUser appUser);
}
