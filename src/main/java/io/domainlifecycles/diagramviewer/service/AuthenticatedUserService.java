package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import java.util.Set;

public interface AuthenticatedUserService {

    boolean userKnown(final String userEmailAddress);

    AuthenticatedUser get(final String userEmailAddress);

    AuthenticatedUser createUser(String userEmailAddress, String fullName);

    AuthenticatedUser createUser(String userEmailAddress, String fullName, Set<Project> projects);
}
