package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser.AppUserBuilder;
import io.domainlifecycles.diagramviewer.model.viewer.IdentityProvider;
import io.domainlifecycles.diagramviewer.model.viewer.LocalCredential;
import io.domainlifecycles.diagramviewer.model.viewer.UserIdentity;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository repository;

    public AppUserServiceImpl(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean userKnownAndActive(final String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress)
            .filter(appUser -> UserStatus.ACTIVE.equals(appUser.getStatus()))
            .isPresent();
    }

    @Override
    public AppUser get(final String userEmailAddress) {
        return repository.getByEmailAddress(userEmailAddress);
    }

    @Override
    public Optional<AppUser> find(final String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress);
    }

    @Override
    public Optional<AppUser> findByApiKey(String apiKey) {
        if(apiKey == null || apiKey.isBlank()) return Optional.empty();
        return repository.findByApiKey(UUID.fromString(apiKey));
    }

    @Override
    public AppUser createInvitedUser(String emailAddress) {
        AppUser appUser = AppUser.builder()
            .emailAddress(emailAddress)
            .status(UserStatus.INVITED)
            .build();

        return repository.save(appUser);
    }

    @Override
    public AppUser createOktaUser(String userEmailAddress, String firstName, String lastName, String sub) {
        UserIdentity oktaUserIdentity = UserIdentity.builder()
            .provider(IdentityProvider.OKTA)
            .externalSubject(sub)
            .build();

        final AppUser appUser = AppUser.builder()
            .emailAddress(userEmailAddress)
            .firstName(firstName)
            .lastName(lastName)
            .apiKey(UUID.randomUUID())
            .status(UserStatus.ACTIVE)
            .build();

        appUser.addUserIdentity(oktaUserIdentity);

        return repository.save(appUser);
    }

    @Override
    public AppUser createSelfServiceUser(String userEmailAddress, String firstName, String lastName, String passwordHash) {

        Optional<AppUser> foundAppUser = find(userEmailAddress);
        checkSelfServiceUserWithMailAlreadyExists(userEmailAddress, foundAppUser);

        UserIdentity localUserIdentity = UserIdentity.builder()
            .provider(IdentityProvider.LOCAL)
            .externalSubject(UUID.randomUUID().toString())
            .build();

        LocalCredential localCredential = LocalCredential.builder()
            .identity(localUserIdentity)
            .passwordHash(passwordHash)
            .build();

        localUserIdentity.setLocalCredential(localCredential);

        AppUser.AppUserBuilder<?, ?> appUserBuilder = AppUser.builder();
        if(foundAppUser.isPresent()) {
            // reuse properties of found user to keep assigned projects
            appUserBuilder = foundAppUser.get().toBuilder();
        }

        return createActiveUserWithNewUserIdentity(userEmailAddress, firstName, lastName, appUserBuilder, localUserIdentity);
    }

    @Override
    public AppUser activateOktaUser(AppUser appUser, String firstName, String lastName, String sub) {
        UserIdentity oktaUserIdentity = UserIdentity.builder()
            .externalSubject(sub)
            .provider(IdentityProvider.OKTA)
            .build();

        return createActiveUserWithNewUserIdentity(appUser.getEmailAddress(), firstName, lastName, appUser.toBuilder(), oktaUserIdentity);
    }

    private AppUser createActiveUserWithNewUserIdentity(String userEmailAddress, String firstName, String lastName, AppUserBuilder<?, ?> appUserBuilder, UserIdentity userIdentity) {
        AppUser appUser = appUserBuilder
            .emailAddress(userEmailAddress)
            .firstName(firstName)
            .lastName(lastName)
            .apiKey(UUID.randomUUID())
            .status(UserStatus.ACTIVE)
            .build();

        appUser.addUserIdentity(userIdentity);

        return repository.save(appUser);
    }

    private void checkSelfServiceUserWithMailAlreadyExists(String userEmailAddress, Optional<AppUser> foundAppUser) {
        boolean userHasSelfServiceAccount = foundAppUser
            .map(user -> user.getIdentities().stream()
                .anyMatch(id -> id.getProvider() == IdentityProvider.LOCAL))
            .orElse(false);

        if(userHasSelfServiceAccount) {
            throw DiagramViewerException.fail(
                String.format("E-Mail '%s' is already taken. Please choose a different mail or sign in to your account.",
                    userEmailAddress));
        }
    }
}
