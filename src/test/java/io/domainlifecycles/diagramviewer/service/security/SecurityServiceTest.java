package io.domainlifecycles.diagramviewer.service.security;

import com.sun.security.auth.UserPrincipal;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.AppUserService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.service.SecurityServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.not;
import static org.mockito.ArgumentMatchers.anyString;
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
        securityService = new SecurityServiceImpl(appUserService, new BCryptPasswordEncoder());
    }

    @Test
    void Should_GetCurrentlySignedInUser_When_UserIsOAuthAndEmailAttributeIsPresent() {

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
    void Should_GetCurrentlySignedInUser_When_UserIsOAuthAndUsernameAttributeIsPresent() {

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
    void Should_GetCurrentlySignedInUser_When_UserIsNotOAuth() {

        // given
        String emailAddress = "test-email@gmail.com";

        AppUser appUserMock = mock(AppUser.class);
        when(appUserService.get(eq(emailAddress))).thenReturn(appUserMock);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(new UserPrincipal(emailAddress), null));

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
    void Should_ReturnUserOnAuthentication_When_UserIsPresentAndActive() {

        // given
        String emailAddress = "test-email@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String sub = "iuawbdhviouadwbvuowabq";

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getStatus()).thenReturn(UserStatus.ACTIVE);

        when(appUserService.find(eq(emailAddress))).thenReturn(Optional.of(appUserMock));

        // when
        AppUser result = securityService.acknowledgeOktaUserAuthentication(emailAddress, firstName, lastName, sub);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(appUserService, times(1)).find(eq(emailAddress));
    }

    @Test
    void Should_ActivateUserOnAuthentication_When_UserIsPresentAndInvited() {

        // given
        String emailAddress = "test-email@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String sub = "iuawbdhviouadwbvuowabq";

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getStatus()).thenReturn(UserStatus.INVITED);

        when(appUserService.find(eq(emailAddress))).thenReturn(Optional.of(appUserMock));
        when(appUserService.activateOktaUser(eq(appUserMock), eq(firstName), eq(lastName), eq(sub))).thenReturn(appUserMock);

        // when
        AppUser result = securityService.acknowledgeOktaUserAuthentication(emailAddress, firstName, lastName, sub);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(appUserService, times(1)).find(eq(emailAddress));
        verify(appUserService, times(1)).activateOktaUser(eq(appUserMock), eq(firstName), eq(lastName), eq(sub));
    }

    @Test
    void Should_CreateOktaUserOnAuthentication_When_UserIsNotPresent() {

        // given
        String emailAddress = "test-email@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String sub = "iuawbdhviouadwbvuowabq";

        AppUser appUserMock = mock(AppUser.class);

        when(appUserService.find(eq(emailAddress))).thenReturn(Optional.empty());
        when(appUserService.createOktaUser(eq(emailAddress), eq(firstName), eq(lastName), eq(sub))).thenReturn(appUserMock);

        // when
        AppUser result = securityService.acknowledgeOktaUserAuthentication(emailAddress, firstName, lastName, sub);

        // then
        assertThat(result).isEqualTo(appUserMock);
        verify(appUserService, times(1)).find(eq(emailAddress));
        verify(appUserService, times(1)).createOktaUser(eq(emailAddress), eq(firstName), eq(lastName), eq(sub));
    }

    @Test
    void Should_RegisterInternalUser() {

        // given
        String emailAddress = "test-email@gmail.com";
        String firstName = "Max";
        String lastName = "Mustermann";
        String password = "testPassword";

        when(appUserService.createInternalUser(eq(emailAddress), eq(firstName), eq(lastName), anyString()))
            .thenReturn(mock(AppUser.class));

        // when
        securityService.registerInternalUser(emailAddress, firstName, lastName, password);

        // then
        verify(appUserService, times(1)).createInternalUser(eq(emailAddress), eq(firstName), eq(lastName), anyString());
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
}
