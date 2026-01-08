package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;

    public SecurityServiceImpl(AppUserService appUserService,
                               PasswordEncoder passwordEncoder) {
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AppUser getCurrentlySignedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = getEmailOfOAuth2User(oAuth2User);
            return appUserService.get(email);
        }

        return appUserService.get(authentication.getName());
    }

    @Override
    public AppUser acknowledgeOktaUserAuthentication(String userEmailAddress, String firstName, String lastName, String sub) {
        Optional<AppUser> foundUser = appUserService.find(userEmailAddress);

        if (foundUser.isPresent()) {
            // Okta User is either active already or has been invited to at least one project
            return getUserWhenActiveOrActivateUser(firstName, lastName, sub, foundUser.get());
        } else {
            // Okta User is new to the system
            return appUserService.createOktaUser(userEmailAddress, firstName, lastName, sub);
        }
    }

    private AppUser getUserWhenActiveOrActivateUser(String firstName, String lastName, String sub, AppUser oktaUser) {
        if (UserStatus.ACTIVE.equals(oktaUser.getStatus())) {
            return oktaUser;
        }
        else {
            return appUserService.activateOktaUser(oktaUser, firstName, lastName, sub);
        }
    }

    @Override
    public void registerInternalUser(String email, String firstName, String lastName, String rawPassword) {
        appUserService.createInternalUser(email, firstName, lastName, passwordEncoder.encode(rawPassword));
    }

    @Override
    public boolean checkAccess(String projectName, AppUser appUser) {
        if (appUser == null || appUser.getAssignedProjects() == null) return false;

        return appUser.getAssignedProjects().stream()
            .anyMatch(project -> Objects.equals(project.getName(), projectName));
    }

    private String getEmailOfOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            email = oAuth2User.getAttribute("preferred_username");
        }

        if (email == null) {
            throw DiagramViewerException.fail("Email or username not found in OAuth2 response.");
        }
        return email;
    }
}
