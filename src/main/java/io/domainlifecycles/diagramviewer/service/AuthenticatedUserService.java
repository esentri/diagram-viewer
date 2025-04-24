package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import java.util.List;
import java.util.Optional;

public interface AuthenticatedUserService {

    boolean userKnown(final String userEmailAddress);

    AuthenticatedUser get(final String userEmailAddress);

    Optional<AuthenticatedUser> findByApiKey(final String apiKey);

    AuthenticatedUser createUser(String userEmailAddress, String fullName);

    void generateApiKeyForUser(AuthenticatedUser authenticatedUser);
}
