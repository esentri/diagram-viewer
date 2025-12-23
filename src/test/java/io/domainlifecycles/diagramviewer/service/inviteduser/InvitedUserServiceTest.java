package io.domainlifecycles.diagramviewer.service.inviteduser;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.InvitedUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import io.domainlifecycles.diagramviewer.service.InvitedUserService;
import io.domainlifecycles.diagramviewer.service.InvitedUserServiceImpl;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitedUserServiceTest {

    @Captor
    ArgumentCaptor<InvitedUser> invitedUserArgumentCaptor;

    @Mock
    InvitedUserRepository repository;

    InvitedUserService service;

    @BeforeEach
    void setUp() {
        service = new InvitedUserServiceImpl(repository);
    }

    @Test
    void Should_KnowUser_When_UserIsInDatabase() {

        // given
        String emailAddress = "test-mail@gmail.com";

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.of(mock(InvitedUser.class)));

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
    void Should_GetUser_When_UserIsInDatabase() {

        // given
        String emailAddress = "test-mail@gmail.com";
        InvitedUser invitedUserMock = mock(InvitedUser.class);

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.of(invitedUserMock));

        // when
        InvitedUser result = service.getOrCreate(emailAddress);

        // then
        assertThat(result).isEqualTo(invitedUserMock);
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
    }

    @Test
    void Should_CreateUser_When_UserIsNotInDatabase() {

        // given
        String emailAddress = "test-mail@gmail.com";
        InvitedUser invitedUserMock = mock(InvitedUser.class);

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.empty());
        when(repository.save(any(InvitedUser.class))).thenReturn(invitedUserMock);

        // when
        InvitedUser result = service.getOrCreate(emailAddress);

        // then
        assertThat(result).isEqualTo(invitedUserMock);
        verify(repository, times(1)).findByEmailAddress(eq(emailAddress));
        verify(repository, times(1)).save(invitedUserArgumentCaptor.capture());
        assertThat(invitedUserArgumentCaptor.getValue().getEmailAddress()).isEqualTo(emailAddress);
        assertThat(invitedUserArgumentCaptor.getValue().getAssignedProjects()).isEmpty();
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnGet_When_UserIsNotInDatabase() {

        // given
        String emailAddress = "test-mail@gmail.com";

        when(repository.findByEmailAddress(eq(emailAddress))).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> service.get(emailAddress))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("No Invited-User found with E-Mail address '" + emailAddress + "'.");
    }

    @Test
    void Should_DeleteUserAndUnassignAllProjects_When_UserIsDeleted() {

        // given
        InvitedUser invitedUserMock = mock(InvitedUser.class);
        Project firstProjectMock = mock(Project.class);
        Project secondProjectMock = mock(Project.class);

        when(invitedUserMock.getAssignedProjects()).thenReturn(Set.of(firstProjectMock, secondProjectMock));
        doNothing().when(repository).delete(invitedUserMock);

        // when
        service.delete(invitedUserMock);

        // then
        verify(firstProjectMock, times(1)).unassignUser(invitedUserMock);
        verify(secondProjectMock, times(1)).unassignUser(invitedUserMock);
        verify(repository, times(1)).delete(invitedUserMock);
    }

    @Test
    void Should_BeReadyForRemoval_When_UserHasNoAssignedProjects() {

        // given
        InvitedUser invitedUserMock = mock(InvitedUser.class);
        when(invitedUserMock.getAssignedProjects()).thenReturn(Collections.emptySet());

        // when
        boolean result = service.checkForRemoval(invitedUserMock);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void Should_NotBeReadyForRemoval_When_UserHasAssignedProjects() {

        // given
        InvitedUser invitedUserMock = mock(InvitedUser.class);
        when(invitedUserMock.getAssignedProjects()).thenReturn(Set.of(mock(Project.class)));

        // when
        boolean result = service.checkForRemoval(invitedUserMock);

        // then
        assertThat(result).isFalse();
    }
}