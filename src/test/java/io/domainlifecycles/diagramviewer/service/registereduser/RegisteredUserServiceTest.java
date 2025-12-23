package io.domainlifecycles.diagramviewer.service.registereduser;

import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import io.domainlifecycles.diagramviewer.service.RegisteredUserService;
import io.domainlifecycles.diagramviewer.service.RegisteredUserServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisteredUserServiceTest {

    @Captor
    ArgumentCaptor<RegisteredUser> registeredUserArgumentCaptor;

    @Mock
    RegisteredUserRepository repository;

    RegisteredUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisteredUserServiceImpl(repository);
    }

    @Test
    void Should_KnowUser_When_UserIsInDatabase() {

        // given
        String emailAddress = "test-mail@gmail.com";

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.of(mock(RegisteredUser.class)));

        // when
        boolean result = service.userKnown(emailAddress);

        // then
        assertThat(result).isTrue();
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
    }

    @Test
    void Should_NotKnowUser_When_UserIsNotInDatabase() {

        // given
        String emailAddress = "test-mail@gmail.com";

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.empty());

        // when
        boolean result = service.userKnown(emailAddress);

        // then
        assertThat(result).isFalse();
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
    }

    @Test
    void Should_FindUserByApiKey_When_UserWithApiKeyIsInDatabase() {

        // given
        UUID apiKey = new UUID(0, 0);
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);

        when(repository.findByApiKey(eq(apiKey))).thenReturn(Optional.of(registeredUserMock));

        // when
        Optional<RegisteredUser> result = service.findByApiKey(apiKey.toString());

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(registeredUserMock);
        verify(repository, times(1)).findByApiKey(eq(apiKey));
    }

    @Test
    void Should_ReturnEmptyOptionalOnFindByApiKey_When_ApiKeyIsNull() {

        // when
        Optional<RegisteredUser> result = service.findByApiKey(null);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void Should_ReturnEmptyOptionalOnFindByApiKey_When_ApiKeyIsBlank() {

        // when
        Optional<RegisteredUser> result = service.findByApiKey("");

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void Should_CreateUser() {

        // given
        String emailAddress = "test-mail@gmail.com";
        String fullName = "Max Mustermann";

        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        when(repository.save(any())).thenReturn(registeredUserMock);

        // when
        RegisteredUser result = service.createUser(emailAddress, fullName);

        // then
        assertThat(result).isEqualTo(registeredUserMock);
        verify(repository, times(1)).save(registeredUserArgumentCaptor.capture());
        assertThat(registeredUserArgumentCaptor.getValue().getEmailAddress()).isEqualTo(emailAddress);
        assertThat(registeredUserArgumentCaptor.getValue().getFullName()).isEqualTo(fullName);
        assertThat(registeredUserArgumentCaptor.getValue().getAssignedProjects()).isEmpty();
    }
}