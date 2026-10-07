/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
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
    private final ProjectRepository projectRepository;

    public SecurityServiceImpl(AppUserService appUserService,
                               PasswordEncoder passwordEncoder,
                               ProjectRepository projectRepository) {
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
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
        // queried: the signed in user is the principal of the session, loaded at login - without its projects, and
        // it would not know projects assigned since
        return appUser != null && appUser.getId() != null
            && projectRepository.existsByNameAndAssignedUsersId(projectName, appUser.getId());
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
