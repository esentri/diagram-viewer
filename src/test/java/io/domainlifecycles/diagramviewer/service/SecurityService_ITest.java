package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.configuration.TestContainersInitializer;
import io.domainlifecycles.diagramviewer.model.InvitedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@ExtendWith(TestContainersInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = TestContainersInitializer.class)
class SecurityService_ITest {

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
        assertThat(newRegisteredUser.getAssignedProjects().get(0).getId()).isEqualTo(project.getId());
        assertThat(newRegisteredUser.getAssignedProjects().get(0).getAssignedRegisteredUsers().size()).isEqualTo(2);
        assertThat(newRegisteredUser.getAssignedProjects().get(0).getAssignedInvitedUsers()).isEmpty();
        assertThat(newRegisteredUser.getAssignedProjects().get(0).getCreator().getId()).isEqualTo(project.getCreator().getId());

        assertThat(invitedUserRepository.findByEmailAddress(INVITED_USER_MAIL_ADDRESS)).isEmpty();
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .boundedContextPackages(List.of("io.esentri.domain"))
            .assignedRegisteredUsers(new ArrayList<>(List.of(registeredUser)))
            .creator(registeredUser)
            .build();

        return projectRepository.save(project);
    }

    private void setUpInvitedUser(Project project) {
        InvitedUser invitedUser = InvitedUser.builder()
            .fullName(INVITED_USER_FULL_NAME)
            .emailAddress(INVITED_USER_MAIL_ADDRESS)
            .build();

        invitedUserRepository.save(invitedUser);
        projectService.assignUser(project, INVITED_USER_MAIL_ADDRESS);
    }
}