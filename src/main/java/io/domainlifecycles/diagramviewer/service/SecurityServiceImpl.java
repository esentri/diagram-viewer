package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.InvitedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.RegisteredUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final RegisteredUserService registeredUserService;
    private final InvitedUserService invitedUserService;
    private final ProjectService projectService;

    public SecurityServiceImpl(RegisteredUserService registeredUserService, InvitedUserService invitedUserService, ProjectService projectService) {
        this.registeredUserService = registeredUserService;
        this.invitedUserService = invitedUserService;
        this.projectService = projectService;
    }

    @Override
    public RegisteredUser getCurrentlySignedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            email = oAuth2User.getAttribute("preferred_username");
        }

        if (email == null) {
            throw DiagramViewerException.fail("Email or username not found in OAuth2 response.");
        }

        return registeredUserService.get(email);
    }

    @Override
    public RegisteredUser acknowledgeUserAuthentication(String userEmailAddress, String fullName) {
        if(registeredUserService.userKnown(userEmailAddress)) return registeredUserService.get(userEmailAddress);

        if(!invitedUserService.userKnown(userEmailAddress)) {
            return registeredUserService.createUser(userEmailAddress, fullName);
        }

        final InvitedUser invitedUser = invitedUserService.get(userEmailAddress);
        List<Project> projectsWithUserAssigned = new ArrayList<>(invitedUser.getAssignedProjects());

        RegisteredUser newRegisteredUser = registeredUserService.createUser(userEmailAddress, fullName);

        projectsWithUserAssigned.forEach(project -> {
            projectService.unassignUser(project, invitedUser);
            projectService.assignUser(project, newRegisteredUser);
        });

        return newRegisteredUser;
    }

    @Override
    public boolean checkAccess(String projectName, RegisteredUser registeredUser) {
        if(registeredUser == null || registeredUser.getAssignedProjects() == null) return false;

        return registeredUser.getAssignedProjects().stream()
            .anyMatch(project -> Objects.equals(project.getName(), projectName));
    }
}
