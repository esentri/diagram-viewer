package io.domainlifecycles.diagramviewer.security;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.IdentityProvider;
import io.domainlifecycles.diagramviewer.model.viewer.LocalCredential;
import io.domainlifecycles.diagramviewer.model.viewer.UserIdentity;
import io.domainlifecycles.diagramviewer.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserService appUserService;

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUser user = appUserService.find(email).orElseThrow(() -> new UsernameNotFoundException(email));

        UserIdentity localIdentity = user.getIdentities().stream()
            .filter(i -> i.getProvider() == IdentityProvider.LOCAL)
            .findFirst()
            .orElseThrow(() -> new UsernameNotFoundException(email));

        LocalCredential localCredential = localIdentity.getLocalCredential();
        if (localCredential == null) {
            throw new UsernameNotFoundException(email);
        }

        return User
            .withUsername(user.getEmailAddress())
            .password(localCredential.getPasswordHash())
            .authorities("ROLE_USER")
            .build();
    }
}