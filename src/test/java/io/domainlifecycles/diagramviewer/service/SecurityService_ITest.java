package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.configuration.TestContainersInitializer;
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.repository.AuthenticatedUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.TemporaryUserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@ExtendWith(TestContainersInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = TestContainersInitializer.class)
class SecurityService_ITest {

    private static final String TEST_USER_FULL_NAME = "Max Mustermann";
    private static final String TEST_USER_MAIL_ADDRESS = "max.mustermann@gmail.com";

    private static final String TEMPORARY_USER_FULL_NAME = "Moritz Mustermann";
    private static final String TEMPORARY_USER_MAIL_ADDRESS = "moritz.mustermann@gmail.com";


    @Autowired
    private SecurityService service;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AuthenticatedUserRepository authenticatedUserRepository;

    @Autowired
    private TemporaryUserRepository temporaryUserRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = AuthenticatedUser.builder()
            .fullName(TEST_USER_FULL_NAME)
            .emailAddress(TEST_USER_MAIL_ADDRESS)
            .build();

        authenticatedUserRepository.save(authenticatedUser);
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        authenticatedUserRepository.deleteAll();
        temporaryUserRepository.deleteAll();
    }

    @Test
    void Should_CreateNewAuthenticatedUser_When_UserSignsUpAndHasNoTemporaryUser() {

        // given
        final String newUserMailAddress = "mika.mustermann@gmail.com";
        final String newUserFullName = "Mika Mustermann";

        // when
        AuthenticatedUser newAuthenticatedUser = service.acknowledgeUserAuthentication(newUserMailAddress,
            newUserFullName);

        // then
        assertThat(newAuthenticatedUser).isNotNull();
        assertThat(newAuthenticatedUser.getEmailAddress()).isEqualTo(newUserMailAddress);
        assertThat(newAuthenticatedUser.getFullName()).isEqualTo(newUserFullName);
    }

    @Test
    void Should_ReturnExistingAuthenticatedUser_When_UserSignsUpAndAlreadyHasAuthenticatedUser() {

        // when
        AuthenticatedUser newAuthenticatedUser = service.acknowledgeUserAuthentication(TEMPORARY_USER_MAIL_ADDRESS,
            TEMPORARY_USER_FULL_NAME);

        // then
        assertThat(newAuthenticatedUser).isNotNull();
        assertThat(newAuthenticatedUser.getEmailAddress()).isEqualTo(TEMPORARY_USER_MAIL_ADDRESS);
        assertThat(newAuthenticatedUser.getFullName()).isEqualTo(TEMPORARY_USER_FULL_NAME);
    }

    @Test
    void Should_ReturnNewAuthenticatedUserAndRemoveTemporaryUser_When_UserSignsUpAndAlreadyHasTemporaryUser() {

        // given
        Project project = setUpProject();
        setUpTemporaryUser(project);

        // when
        AuthenticatedUser newAuthenticatedUser = service.acknowledgeUserAuthentication(TEMPORARY_USER_MAIL_ADDRESS,
            TEMPORARY_USER_FULL_NAME);

        // then
        assertThat(newAuthenticatedUser).isNotNull();
        assertThat(newAuthenticatedUser.getEmailAddress()).isEqualTo(TEMPORARY_USER_MAIL_ADDRESS);
        assertThat(newAuthenticatedUser.getFullName()).isEqualTo(TEMPORARY_USER_FULL_NAME);
        assertThat(newAuthenticatedUser.getAssignedProjects().get(0).getId()).isEqualTo(project.getId());
        assertThat(newAuthenticatedUser.getAssignedProjects().get(0).getAssignedAuthenticatedUsers().size()).isEqualTo(2);
        assertThat(newAuthenticatedUser.getAssignedProjects().get(0).getAssignedTemporaryUsers()).isEmpty();
        assertThat(newAuthenticatedUser.getAssignedProjects().get(0).getCreator().getId()).isEqualTo(project.getCreator().getId());

        assertThat(temporaryUserRepository.findByEmailAddress(TEMPORARY_USER_MAIL_ADDRESS)).isEmpty();
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .boundedContextPackages(List.of("io.esentri.domain"))
            .assignedAuthenticatedUsers(new ArrayList<>(List.of(authenticatedUser)))
            .creator(authenticatedUser)
            .build();

        return projectRepository.save(project);
    }

    private void setUpTemporaryUser(Project project) {
        TemporaryUser temporaryUser = TemporaryUser.builder()
            .fullName(TEMPORARY_USER_FULL_NAME)
            .emailAddress(TEMPORARY_USER_MAIL_ADDRESS)
            .build();

        temporaryUserRepository.save(temporaryUser);
        projectService.assignUser(project, TEMPORARY_USER_MAIL_ADDRESS);
    }
}