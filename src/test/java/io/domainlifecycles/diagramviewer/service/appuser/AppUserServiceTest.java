package io.domainlifecycles.diagramviewer.service.appuser;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.IdentityProvider;
import io.domainlifecycles.diagramviewer.model.viewer.LocalCredential;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.UserIdentity;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.service.AppUserService;
import io.domainlifecycles.diagramviewer.service.AppUserServiceImpl;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Captor
    ArgumentCaptor<AppUser> appUserArgumentCaptor;

    @Mock
    AppUserRepository repository;

    AppUserService service;

    @BeforeEach
    void setUp() {
        service = new AppUserServiceImpl(repository);
    }

    @Test
    void Should_KnowUser_When_UserIsInDatabaseAndActive() {

        // given
        String emailAddress = "test-mail@gmail.com";

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getStatus()).thenReturn(UserStatus.ACTIVE);

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.of(appUserMock));

        // when
        boolean result = service.userKnownAndActive(emailAddress);

        // then
        assertThat(result).isTrue();
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
    }

    @Test
    void Should_NotKnowUser_When_UserIsInDatabaseAndInvited() {

        // given
        String emailAddress = "test-mail@gmail.com";

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getStatus()).thenReturn(UserStatus.INVITED);

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.of(appUserMock));

        // when
        boolean result = service.userKnownAndActive(emailAddress);

        // then
        assertThat(result).isFalse();
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
    }

    @Test
    void Should_NotKnowUser_When_UserIsNotInDatabase() {

        // given
        String emailAddress = "test-mail@gmail.com";

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.empty());

        // when
        boolean result = service.userKnownAndActive(emailAddress);

        // then
        assertThat(result).isFalse();
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
    }

    @Test
    void Should_GetUser() {

        // given
        String emailAddress = "test-mail@gmail.com";

        AppUser appUserMock = mock(AppUser.class);
        when(repository.getByEmailAddress(eq(emailAddress))).thenReturn(appUserMock);

        // when
        AppUser result = service.get(emailAddress);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(repository, times(1)).getByEmailAddress(eq(emailAddress));
    }

    @Test
    void Should_FindUser() {

        // given
        String emailAddress = "test-mail@gmail.com";

        AppUser appUserMock = mock(AppUser.class);
        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.of(appUserMock));

        // when
        Optional<AppUser> result = service.find(emailAddress);

        // then
        assertThat(result).contains(appUserMock);
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
    }

    @Test
    void Should_FindUserByApiKey_When_UserWithApiKeyIsInDatabase() {

        // given
        UUID apiKey = new UUID(0, 0);
        AppUser appUserMock = mock(AppUser.class);

        when(repository.findByApiKey(eq(apiKey))).thenReturn(Optional.of(appUserMock));

        // when
        Optional<AppUser> result = service.findByApiKey(apiKey.toString());

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(appUserMock);
        verify(repository, times(1)).findByApiKey(eq(apiKey));
    }

    @Test
    void Should_ReturnEmptyOptionalOnFindByApiKey_When_ApiKeyIsNull() {

        // when
        Optional<AppUser> result = service.findByApiKey(null);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void Should_ReturnEmptyOptionalOnFindByApiKey_When_ApiKeyIsBlank() {

        // when
        Optional<AppUser> result = service.findByApiKey("");

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void Should_CreateInvitedUser() {

        // given
        String emailAddress = "test-mail@gmail.com";

        AppUser appUserMock = mock(AppUser.class);
        when(repository.save(any(AppUser.class))).thenReturn(appUserMock);

        // when
        AppUser result = service.createInvitedUser(emailAddress);

        // then
        verify(repository, times(1)).save(appUserArgumentCaptor.capture());
        assertThat(appUserArgumentCaptor.getValue().getEmailAddress()).isEqualTo(emailAddress);
        assertThat(appUserArgumentCaptor.getValue().getStatus()).isEqualTo(UserStatus.INVITED);
        assertThat(result).isEqualTo(appUserMock);
    }

    @Test
    void Should_CreateOktaUser() {

        // given
        String emailAddress = "test-mail@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String sub = "poivnwiopvneivn";

        AppUser appUserMock = mock(AppUser.class);
        when(repository.save(any(AppUser.class))).thenReturn(appUserMock);

        // when
        AppUser result = service.createOktaUser(emailAddress, firstName, lastName, sub);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(repository, times(1)).save(appUserArgumentCaptor.capture());
        AppUser capturedAppUser = appUserArgumentCaptor.getValue();
        assertThat(capturedAppUser.getEmailAddress()).isEqualTo(emailAddress);
        assertThat(capturedAppUser.getFirstName()).isEqualTo(firstName);
        assertThat(capturedAppUser.getLastName()).isEqualTo(lastName);
        assertThat(capturedAppUser.getApiKey()).isNotNull();
        assertThat(capturedAppUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        assertThat(capturedAppUser.getIdentities()).hasSize(1);

        UserIdentity userIdentity = capturedAppUser.getIdentities().stream().findFirst().get();
        assertThat(userIdentity.getUser()).isEqualTo(capturedAppUser);
        assertThat(userIdentity.getProvider()).isEqualTo(IdentityProvider.OKTA);
        assertThat(userIdentity.getExternalSubject()).isEqualTo(sub);

        assertThat(capturedAppUser.getAssignedProjects()).isEmpty();
    }

    @Test
    void Should_CreateSelfServiceUser_When_NoUserWithEmailExists() {

        // given
        String emailAddress = "test-mail@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String passwordHash = "someHashedPassword";

        AppUser appUserMock = mock(AppUser.class);
        when(repository.save(any(AppUser.class))).thenReturn(appUserMock);
        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.empty());

        // when
        AppUser result = service.createSelfServiceUser(emailAddress, firstName, lastName, passwordHash);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
        verify(repository, times(1)).save(appUserArgumentCaptor.capture());
        AppUser capturedAppUser = appUserArgumentCaptor.getValue();
        assertThat(capturedAppUser.getEmailAddress()).isEqualTo(emailAddress);
        assertThat(capturedAppUser.getFirstName()).isEqualTo(firstName);
        assertThat(capturedAppUser.getLastName()).isEqualTo(lastName);
        assertThat(capturedAppUser.getApiKey()).isNotNull();
        assertThat(capturedAppUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        assertThat(capturedAppUser.getIdentities()).hasSize(1);

        UserIdentity userIdentity = capturedAppUser.getIdentities().stream().findFirst().get();
        assertThat(userIdentity.getUser()).isEqualTo(capturedAppUser);
        assertThat(userIdentity.getProvider()).isEqualTo(IdentityProvider.LOCAL);
        assertThat(userIdentity.getExternalSubject()).isNotNull();

        LocalCredential localCredential = userIdentity.getLocalCredential();
        assertThat(localCredential.getIdentity()).isEqualTo(userIdentity);
        assertThat(localCredential.getPasswordHash()).isEqualTo(passwordHash);

        assertThat(capturedAppUser.getAssignedProjects()).isEmpty();
    }

    @Test
    void Should_CreateSelfServiceUser_When_NoUserWithEmailButOktaIdentityExists() {

        // given
        String emailAddress = "test-mail@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String passwordHash = "someHashedPassword";

        AppUser existingAppUser = AppUser.builder()
            .identities(new HashSet<>(Set.of(
                UserIdentity.builder()
                    .provider(IdentityProvider.OKTA)
                    .build()
            )))
            .build();
        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.of(existingAppUser));

        AppUser appUserMock = mock(AppUser.class);
        when(repository.save(any(AppUser.class))).thenReturn(appUserMock);

        // when
        AppUser result = service.createSelfServiceUser(emailAddress, firstName, lastName, passwordHash);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
        verify(repository, times(1)).save(appUserArgumentCaptor.capture());

        AppUser capturedAppUser = appUserArgumentCaptor.getValue();
        assertThat(capturedAppUser.getEmailAddress()).isEqualTo(emailAddress);
        assertThat(capturedAppUser.getFirstName()).isEqualTo(firstName);
        assertThat(capturedAppUser.getLastName()).isEqualTo(lastName);
        assertThat(capturedAppUser.getApiKey()).isNotNull();
        assertThat(capturedAppUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        assertThat(capturedAppUser.getIdentities()).hasSize(2);

        UserIdentity localUserIdentity = capturedAppUser.getIdentities()
            .stream()
            .filter(identity -> identity.getProvider().equals(IdentityProvider.LOCAL))
            .findFirst().get();

        assertThat(localUserIdentity.getUser()).isEqualTo(capturedAppUser);
        assertThat(localUserIdentity.getProvider()).isEqualTo(IdentityProvider.LOCAL);
        assertThat(localUserIdentity.getExternalSubject()).isNotNull();

        LocalCredential localCredential = localUserIdentity.getLocalCredential();
        assertThat(localCredential.getIdentity()).isEqualTo(localUserIdentity);
        assertThat(localCredential.getPasswordHash()).isEqualTo(passwordHash);

        assertThat(capturedAppUser.getAssignedProjects()).isEmpty();
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnCreateSelfServiceUser_When_UserWithEmailAndLocalIdentityExists() {

        // given
        String emailAddress = "test-mail@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String passwordHash = "someHashedPassword";

        AppUser existingAppUser = AppUser.builder()
            .identities(new HashSet<>(Set.of(
                UserIdentity.builder()
                    .provider(IdentityProvider.LOCAL)
                    .build()
            )))
            .build();
        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.of(existingAppUser));

        // when
        assertThatThrownBy(() -> service.createSelfServiceUser(emailAddress, firstName, lastName, passwordHash))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("E-Mail 'test-mail@gmail.com' is already taken. Please choose a different mail or sign in to your account.");

        // then
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
        verify(repository, never()).save(any());
    }

    @Test
    void Should_ActivateOktaUser() {

        // given
        String emailAddress = "test-mail@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String sub = "poivnwiopvneivn";

        Project projectMock = mock(Project.class);

        AppUser existingAppUser = AppUser.builder()
            .emailAddress(emailAddress)
            .status(UserStatus.INVITED)
            .assignedProjects(new HashSet<>(Set.of(projectMock)))
            .build();

        // when
        service.activateOktaUser(existingAppUser, firstName, lastName, sub);

        // then
        verify(repository, times(1)).save(appUserArgumentCaptor.capture());

        AppUser savedAppUser = appUserArgumentCaptor.getValue();
        assertThat(savedAppUser.getEmailAddress()).isEqualTo(emailAddress);
        assertThat(savedAppUser.getFirstName()).isEqualTo(firstName);
        assertThat(savedAppUser.getLastName()).isEqualTo(lastName);
        assertThat(savedAppUser.getApiKey()).isNotNull();
        assertThat(savedAppUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        UserIdentity userIdentity = savedAppUser.getIdentities().stream().findFirst().get();
        assertThat(userIdentity.getExternalSubject()).isEqualTo(sub);
        assertThat(userIdentity.getProvider()).isEqualTo(IdentityProvider.OKTA);

        Project project = savedAppUser.getAssignedProjects().stream().findFirst().get();
        assertThat(project).isEqualTo(projectMock);
    }
}