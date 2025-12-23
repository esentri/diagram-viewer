package io.domainlifecycles.diagramviewer.service.security;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.InvitedUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
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

    private static final String REGISTERED_USER_FULL_NAME = "Max Mustermann";
    private static final String REGISTERED_USER_MAIL_ADDRESS = "max.mustermann@gmail.com";

    private static final String INVITED_USER_FULL_NAME = "Moritz Mustermann";
    private static final String INVITED_USER_MAIL_ADDRESS = "moritz.mustermann@gmail.com";


    @Autowired
    private SecurityService service;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private InvitedUserRepository invitedUserRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private RegisteredUser registeredUser;

    @BeforeEach
    void setUp() {
        registeredUser = RegisteredUser.builder()
            .fullName(REGISTERED_USER_FULL_NAME)
            .emailAddress(REGISTERED_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        registeredUserRepository.save(registeredUser);
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        registeredUserRepository.deleteAll();
        invitedUserRepository.deleteAll();
    }

    @Test
    void Should_CreateNewRegisteredUser_When_UserSignsUpAndHasNoInvitedUser() {

        // given
        final String newUserMailAddress = "mika.mustermann@gmail.com";
        final String newUserFullName = "Mika Mustermann";

        // when
        RegisteredUser newRegisteredUser = service.acknowledgeUserAuthentication(newUserMailAddress,
            newUserFullName);

        // then
        assertThat(newRegisteredUser).isNotNull();
        assertThat(newRegisteredUser.getEmailAddress()).isEqualTo(newUserMailAddress);
        assertThat(newRegisteredUser.getFullName()).isEqualTo(newUserFullName);
    }

    @Test
    void Should_ReturnExistingRegisteredUser_When_UserSignsUpAndAlreadyHasRegisteredUser() {

        // when
        RegisteredUser newRegisteredUser = service.acknowledgeUserAuthentication(INVITED_USER_MAIL_ADDRESS,
            INVITED_USER_FULL_NAME);

        // then
        assertThat(newRegisteredUser).isNotNull();
        assertThat(newRegisteredUser.getEmailAddress()).isEqualTo(INVITED_USER_MAIL_ADDRESS);
        assertThat(newRegisteredUser.getFullName()).isEqualTo(INVITED_USER_FULL_NAME);
    }

    @Test
    void Should_ReturnNewRegisteredUserAndRemoveInvitedUser_When_UserSignsUpAndAlreadyHasInvitedUser() {

        // given
        Project project = setUpProject();
        setUpInvitedUser(project);

        // when
        RegisteredUser newRegisteredUser = service.acknowledgeUserAuthentication(INVITED_USER_MAIL_ADDRESS,
            INVITED_USER_FULL_NAME);

        // then
        assertThat(newRegisteredUser).isNotNull();
        assertThat(newRegisteredUser.getEmailAddress()).isEqualTo(INVITED_USER_MAIL_ADDRESS);
        assertThat(newRegisteredUser.getFullName()).isEqualTo(INVITED_USER_FULL_NAME);
        assertThat(newRegisteredUser.getAssignedProjects().stream().findFirst().orElseThrow().getId()).isEqualTo(project.getId());
        assertThat(newRegisteredUser.getAssignedProjects().stream().findFirst().orElseThrow().getAssignedRegisteredUsers().size()).isEqualTo(2);
        assertThat(newRegisteredUser.getAssignedProjects().stream().findFirst().orElseThrow().getAssignedInvitedUsers()).isEmpty();
        assertThat(newRegisteredUser.getAssignedProjects().stream().findFirst().orElseThrow().getCreator().getId()).isEqualTo(project.getCreator().getId());

        assertThat(invitedUserRepository.findByEmailAddress(INVITED_USER_MAIL_ADDRESS)).isEmpty();
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .diagrams(new HashSet<>())
            .assignedRegisteredUsers(new HashSet<>(Set.of(registeredUser)))
            .assignedInvitedUsers(new HashSet<>())
            .creator(registeredUser)
            .build();

        return projectRepository.save(project);
    }

    private void setUpInvitedUser(Project project) {
        InvitedUser invitedUser = InvitedUser.builder()
            .fullName(INVITED_USER_FULL_NAME)
            .emailAddress(INVITED_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        invitedUserRepository.save(invitedUser);
        projectService.assignUser(project, INVITED_USER_MAIL_ADDRESS);
    }
}