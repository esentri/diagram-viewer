package io.domainlifecycles.diagramviewer.service.security;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.IdentityProvider;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.UserIdentity;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityService_ITest extends BaseIntegrationTest {

    private static final String ACTIVE_USER_FIRST_NAME = "Max";
    private static final String ACTIVE_USER_LAST_NAME = "Mustermann";
    private static final String ACTIVE_USER_MAIL_ADDRESS = "max.mustermann@gmail.com";

    private static final String INVITED_USER_FIRST_NAME = "Moritz";
    private static final String INVITED_USER_LAST_NAME = "Mustermann";
    private static final String INVITED_USER_MAIL_ADDRESS = "moritz.mustermann@gmail.com";


    @Autowired
    private SecurityService service;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = AppUser.builder()
            .firstName(ACTIVE_USER_FIRST_NAME)
            .lastName(ACTIVE_USER_LAST_NAME)
            .emailAddress(ACTIVE_USER_MAIL_ADDRESS)
            .status(UserStatus.ACTIVE)
            .build();

        appUserRepository.save(appUser);
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void Should_CreateNewActiveUser_When_UserSignsUpAndHasNoInvitedUser() {

        // given
        final String newUserMailAddress = "mika.mustermann@gmail.com";
        final String newUserFirstName = "Mika";
        final String newUserLastName = "Mustermann";
        final String newUserSub = "aounsvqoiefvn";

        // when
        AppUser newAppUser = service.acknowledgeOktaUserAuthentication(newUserMailAddress,
            newUserFirstName, newUserLastName, newUserSub);

        // then
        assertThat(newAppUser).isNotNull();
        assertThat(newAppUser.getEmailAddress()).isEqualTo(newUserMailAddress);
        assertThat(newAppUser.getFirstName()).isEqualTo(newUserFirstName);
        assertThat(newAppUser.getLastName()).isEqualTo(newUserLastName);
        assertThat(newAppUser.getApiKey()).isNotNull();

        UserIdentity userIdentity = newAppUser.getIdentities().stream().findFirst().get();
        assertThat(userIdentity.getUser()).isEqualTo(newAppUser);
        assertThat(userIdentity.getProvider()).isEqualTo(IdentityProvider.OKTA);
        assertThat(userIdentity.getExternalSubject()).isEqualTo(newUserSub);
        assertThat(userIdentity.getLocalCredential()).isNull();
    }

    @Test
    void Should_ReturnExistingActiveUser_When_UserSignsUpAndAlreadyHasActiveUser() {

        // when
        AppUser newAppUser = service.acknowledgeOktaUserAuthentication(ACTIVE_USER_MAIL_ADDRESS,
            INVITED_USER_FIRST_NAME, ACTIVE_USER_LAST_NAME, "someSub");

        // then
        assertThat(newAppUser).isNotNull();
        assertThat(newAppUser.getEmailAddress()).isEqualTo(ACTIVE_USER_MAIL_ADDRESS);
        assertThat(newAppUser.getFirstName()).isEqualTo(ACTIVE_USER_FIRST_NAME);
        assertThat(newAppUser.getLastName()).isEqualTo(ACTIVE_USER_LAST_NAME);
    }

    @Test
    void Should_ReturnActiveUser_When_UserSignsUpAndAlreadyHasInvitedUser() {

        // given
        final String sub = "aounsvqoiefvn";

        Project project = setUpProject();
        setUpInvitedUser(project);

        // when
        AppUser newAppUser = service.acknowledgeOktaUserAuthentication(INVITED_USER_MAIL_ADDRESS,
            INVITED_USER_FIRST_NAME, INVITED_USER_LAST_NAME, sub);

        // then
        assertThat(newAppUser).isNotNull();
        assertThat(newAppUser.getEmailAddress()).isEqualTo(INVITED_USER_MAIL_ADDRESS);
        assertThat(newAppUser.getFirstName()).isEqualTo(INVITED_USER_FIRST_NAME);
        assertThat(newAppUser.getLastName()).isEqualTo(INVITED_USER_LAST_NAME);
        UserIdentity userIdentity = newAppUser.getIdentities().stream().findFirst().get();
        assertThat(userIdentity.getExternalSubject()).isEqualTo(sub);
        assertThat(userIdentity.getProvider()).isEqualTo(IdentityProvider.OKTA);

        assertThat(newAppUser.getAssignedProjects().stream().findFirst().orElseThrow().getId()).isEqualTo(project.getId());
        assertThat(newAppUser.getAssignedProjects().stream().findFirst().orElseThrow().getAssignedUsers().size()).isEqualTo(2);
        assertThat(newAppUser.getAssignedProjects().stream().findFirst().orElseThrow().getCreator().getId()).isEqualTo(project.getCreator().getId());
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .diagrams(new HashSet<>())
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .creator(appUser)
            .build();

        return projectRepository.save(project);
    }

    private void setUpInvitedUser(Project project) {
        AppUser invitedUser = AppUser.builder()
            .firstName(INVITED_USER_FIRST_NAME)
            .lastName(INVITED_USER_LAST_NAME)
            .emailAddress(INVITED_USER_MAIL_ADDRESS)
            .status(UserStatus.INVITED)
            .assignedProjects(new HashSet<>())
            .build();

        appUserRepository.save(invitedUser);
        projectService.assignUser(project, INVITED_USER_MAIL_ADDRESS);
    }
}
