package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final AuthenticatedUserService authenticatedUserService;
    private final TemporaryUserService temporaryUserService;
    private final ProjectService projectService;

    public SecurityServiceImpl(AuthenticatedUserService authenticatedUserService, TemporaryUserService temporaryUserService, ProjectService projectService) {
        this.authenticatedUserService = authenticatedUserService;
        this.temporaryUserService = temporaryUserService;
        this.projectService = projectService;
    }

    @Override
    public AuthenticatedUser acknowledgeUserAuthentication(String userEmailAddress, String fullName) {
        if(authenticatedUserService.userKnown(userEmailAddress)) return authenticatedUserService.get(userEmailAddress);

        if(!temporaryUserService.userKnown(userEmailAddress)) {
            return authenticatedUserService.createUser(userEmailAddress, fullName);
        }

        final TemporaryUser temporaryUser = temporaryUserService.get(userEmailAddress);

        List<Project> projectsWithUserUnassigned = new ArrayList<>(temporaryUser.getAssignedProjects()).stream().map(
            project -> projectService.unassignUser(project, temporaryUser)).toList();
        return authenticatedUserService.createUser(userEmailAddress, fullName, projectsWithUserUnassigned);
    }

    @Override
    public boolean checkAccess(String projectNameClean, AuthenticatedUser authenticatedUser) {
        if(authenticatedUser == null || authenticatedUser.getAssignedProjects() == null) return false;

        return authenticatedUser.getAssignedProjects().stream()
            .anyMatch(project -> Objects.equals(project.getProjectNameClean(), projectNameClean));
    }
}
