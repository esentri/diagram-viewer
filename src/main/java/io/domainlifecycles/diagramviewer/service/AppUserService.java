package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import java.util.Optional;

public interface AppUserService {

    boolean userKnownAndActive(final String userEmailAddress);

    AppUser get(final String userEmailAddress);

    Optional<AppUser> find(final String userEmailAddress);

    Optional<AppUser> findByApiKey(final String apiKey);

    AppUser createInvitedUser(String emailAddress);

    AppUser createOktaUser(String userEmailAddress, String fullName, String lastName, String sub);

    AppUser createSelfServiceUser(String userEmailAddress, String fullName, String lastName, String passwordHash);

    AppUser activateOktaUser(AppUser appUser, String firstName, String lastName, String sub);
}
