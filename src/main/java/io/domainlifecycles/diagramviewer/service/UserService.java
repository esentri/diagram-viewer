package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.User;

public interface UserService {

    boolean userKnown(final String userEmailAddress);
    User acknowledgeUserAuthentication(String userEmailAddress, String fullName);
    void addProject(User user, Project project);
}
