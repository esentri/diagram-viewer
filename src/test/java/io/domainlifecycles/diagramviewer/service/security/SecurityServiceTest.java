/*package io.domainlifecycles.diagramviewer.service.security;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.InvitedUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.service.InvitedUserService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.AppUserService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.service.SecurityServiceImpl;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
class SecurityServiceTest {

    @Mock
    AppUserService appUserService;

    @Mock
    ProjectService projectService;

    SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityServiceImpl(appUserService, projectService);
    }

    @Test
    void Should_GetCurrentlySignedInUser_When_EmailAttributeIsPresent() {

        // given
        String emailAddress = "test-email@gmail.com";

        OAuth2User oAuth2UserMock = mock(OAuth2User.class);
        when(oAuth2UserMock.getAttribute(eq("email"))).thenReturn(emailAddress);

        AppUser appUserMock = mock(AppUser.class);
        when(appUserService.get(eq(emailAddress))).thenReturn(appUserMock);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(oAuth2UserMock, null));

        // when
        AppUser result = securityService.getCurrentlySignedInUser();

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(appUserService, times(1)).get(eq(emailAddress));
    }

    @Test
    void Should_GetCurrentlySignedInUser_When_UsernameAttributeIsPresent() {

        // given
        String emailAddress = "test-email@gmail.com";

        OAuth2User oAuth2UserMock = mock(OAuth2User.class);
        when(oAuth2UserMock.getAttribute(eq("email"))).thenReturn(null);
        when(oAuth2UserMock.getAttribute(eq("preferred_username"))).thenReturn(emailAddress);

        AppUser appUserMock = mock(AppUser.class);
        when(appUserService.get(eq(emailAddress))).thenReturn(appUserMock);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(oAuth2UserMock, null));

        // when
        AppUser result = securityService.getCurrentlySignedInUser();

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(appUserService, times(1)).get(eq(emailAddress));
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnGetCurrentlySignedInUser_When_NoAttributeIsPresent() {

        // given
        OAuth2User oAuth2UserMock = mock(OAuth2User.class);
        when(oAuth2UserMock.getAttribute(eq("email"))).thenReturn(null);
        when(oAuth2UserMock.getAttribute(eq("preferred_username"))).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(oAuth2UserMock, null));

        // when
        assertThatThrownBy(() -> securityService.getCurrentlySignedInUser())
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("Email or username not found in OAuth2 response.");
    }

    @Test
    void Should_ReturnUserOnAuthentication_When_UserIsKnown() {

        // given
        String emailAddress = "test-email@gmail.com";
        String fullName = "Max Mustermann";

        AppUser appUserMock = mock(AppUser.class);

        when(appUserService.userKnownAndActive(eq(emailAddress))).thenReturn(true);
        when(appUserService.get(eq(emailAddress))).thenReturn(appUserMock);

        // when
        AppUser result = securityService.acknowledgeOAuth2UserAuthentication(emailAddress, fullName);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(appUserService, times(1)).userKnownAndActive(eq(emailAddress));
        verify(appUserService, times(1)).get(eq(emailAddress));
    }

    @Test
    void Should_CreateRegisteredUserOnAuthentication_When_UserIsNotKnown() {

        // given
        String emailAddress = "test-email@gmail.com";
        String fullName = "Max Mustermann";

        AppUser appUserMock = mock(AppUser.class);

        when(appUserService.userKnownAndActive(eq(emailAddress))).thenReturn(false);
        when(invitedUserService.userKnown(eq(emailAddress))).thenReturn(false);
        when(appUserService.createOktaUser(eq(emailAddress), eq(fullName))).thenReturn(appUserMock);

        // when
        AppUser result = securityService.acknowledgeOAuth2UserAuthentication(emailAddress, fullName);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(appUserService, times(1)).userKnownAndActive(eq(emailAddress));
        verify(invitedUserService, times(1)).userKnown(eq(emailAddress));
        verify(appUserService, times(1)).createOktaUser(eq(emailAddress), eq(fullName));
    }

    @Test
    void Should_TransformUserOnAuthentication_When_InvitedUserIsKnown() {

        // given
        String emailAddress = "test-email@gmail.com";
        String fullName = "Max Mustermann";

        InvitedUser invitedUserMock = mock(InvitedUser.class);
        AppUser appUserMock = mock(AppUser.class);

        Project firstProjectMock = mock(Project.class);
        Project secondProjectMock = mock(Project.class);

        when(invitedUserMock.getAssignedProjects()).thenReturn(Set.of(firstProjectMock, secondProjectMock));

        when(appUserService.userKnownAndActive(eq(emailAddress))).thenReturn(false);
        when(invitedUserService.userKnown(eq(emailAddress))).thenReturn(true);
        when(invitedUserService.get(eq(emailAddress))).thenReturn(invitedUserMock);
        when(appUserService.createOktaUser(eq(emailAddress), eq(fullName))).thenReturn(appUserMock);

        doNothing().when(projectService).unassignUser(any(), any());
        doNothing().when(projectService).assignUser(any(), any(AppUser.class));

        // when
        AppUser result = securityService.acknowledgeOAuth2UserAuthentication(emailAddress, fullName);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(appUserService, times(1)).userKnownAndActive(eq(emailAddress));
        verify(invitedUserService, times(1)).userKnown(eq(emailAddress));
        verify(invitedUserService, times(1)).get(eq(emailAddress));
        verify(appUserService, times(1)).createOktaUser(eq(emailAddress), eq(fullName));

        verify(projectService, times(1)).unassignUser(eq(firstProjectMock), eq(invitedUserMock));
        verify(projectService, times(1)).assignUser(eq(firstProjectMock), eq(appUserMock));
        verify(projectService, times(1)).unassignUser(eq(secondProjectMock), eq(invitedUserMock));
        verify(projectService, times(1)).assignUser(eq(secondProjectMock), eq(appUserMock));
    }

    @Test
    void Should_HaveNoAccess_When_UserIsNull() {

        // when
        boolean result = securityService.checkAccess("testProjectName", null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void Should_HaveNoAccess_When_UserHasNoAssignedProjects() {

        // given
        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getAssignedProjects()).thenReturn(Set.of());

        // when
        boolean result = securityService.checkAccess("testProjectName", appUserMock);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void Should_HaveNoAccess_When_UserHasNullAssignedProjects() {

        // given
        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getAssignedProjects()).thenReturn(null);

        // when
        boolean result = securityService.checkAccess("testProjectName", appUserMock);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void Should_HaveNoAccess_When_UserIsNotAssignedToProject() {

        // given
        String anotherProjectName = "anotherProject";

        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn("project");

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getAssignedProjects()).thenReturn(Set.of(projectMock));

        // when
        boolean result = securityService.checkAccess(anotherProjectName, appUserMock);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void Should_HaveAccess_When_UserIsAssignedToProject() {

        // given
        String projectName = "Project";
        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn(projectName);

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getAssignedProjects()).thenReturn(Set.of(projectMock));

        // when
        boolean result = securityService.checkAccess(projectName, appUserMock);

        // then
        assertThat(result).isTrue();
    }
}*/
