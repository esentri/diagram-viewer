package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import java.util.Optional;

public interface RegisteredUserService {

    boolean userKnown(final String userEmailAddress);

    RegisteredUser get(final String userEmailAddress);

    Optional<RegisteredUser> findByApiKey(final String apiKey);

    RegisteredUser createUser(String userEmailAddress, String fullName);

    void generateApiKeyForUser(RegisteredUser registeredUser);
}
