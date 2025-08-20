package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.configuration.TestContainersInitializer;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.InvitedUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
class ProjectService_ITest {

    private static final String REGISTERED_USER_FULL_NAME = "Max Mustermann";
    private static final String REGISTERED_USER_MAIL_ADDRESS = "max.mustermann@gmail.com";

    private static final String INVITED_USER_FULL_NAME = "Moritz Mustermann";
    private static final String INVITED_USER_MAIL_ADDRESS = "moritz.mustermann@gmail.com";

    @Autowired
    ProjectService service;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    RegisteredUserRepository registeredUserRepository;

    @Autowired
    InvitedUserRepository invitedUserRepository;

    @Autowired
    DiagramRepository diagramRepository;

    @Value("${targets.location}") String targetsDirectory;

    @Value("${diagrams.location}") String diagramsLocation;

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
    void Should_CreateProject_When_AllValuesAreValid() throws IOException {
        var pack = new HashSet<String>();
        pack.add("com.esentri");
        // when
        Project project = service.create(registeredUser,
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "test-project-1.0.0-ÄÖÜ.txt", pack);

        // then
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo("test_project_1_0_0_ÄÖÜ_txt");
        assertThat(project.getCreator().getId()).isEqualTo(registeredUser.getId());
        assertThat(project.getAssignedRegisteredUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedRegisteredUsers().stream().findFirst().orElseThrow().getId()).isEqualTo(registeredUser.getId());
        assertThat(project.getCreator().getId()).isEqualTo(registeredUser.getId());
        assertThat(project.getAssignedInvitedUsers()).isEmpty();

        FileIOUtils.deleteDirectoryRecursively(Path.of(targetsDirectory));
    }

    @Test
    void Should_AssignUserToProject_When_UserIsInvited() {

        // given
        Project project = setUpProject();

        // when
        service.assignUser(project, INVITED_USER_MAIL_ADDRESS);

        // then
        assertThat(project.getAssignedInvitedUsers())
            .anySatisfy(invitedUser -> assertThat(invitedUser.getEmailAddress())
                .isEqualTo(INVITED_USER_MAIL_ADDRESS));
        assertThat(project.getAssignedRegisteredUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(REGISTERED_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_AssignUserToProject_When_UserIsRegistered() {

        // given
        Project project = setUpProject();
        RegisteredUser anotherRegisteredUser = setUpRegisteredUser();

        // when
        service.assignUser(project, anotherRegisteredUser.getEmailAddress());

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedInvitedUsers()).isEmpty();
        assertThat(project.getAssignedRegisteredUsers().size()).isEqualTo(2);
        assertThat(project.getAssignedRegisteredUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(REGISTERED_USER_MAIL_ADDRESS));
        assertThat(project.getAssignedRegisteredUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(anotherRegisteredUser.getEmailAddress()));
    }

    @Test
    void Should_UnassignUserFromProject_When_UserIsRegistered() {

        // given
        Project project = setUpProject();
        RegisteredUser anotherRegisteredUser = setUpRegisteredUser();
        Set<RegisteredUser> updatedRegisteredUsers = new HashSet<>(project.getAssignedRegisteredUsers());
        updatedRegisteredUsers.add(anotherRegisteredUser);

        project.setAssignedRegisteredUsers(updatedRegisteredUsers);
        projectRepository.save(project);

        // when
        service.unassignUser(project, anotherRegisteredUser);

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedInvitedUsers()).isEmpty();
        assertThat(project.getAssignedRegisteredUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedRegisteredUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(REGISTERED_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_UnassignUserFromProject_When_UserIsInvited() {

        // given
        Project project = setUpProject();
        InvitedUser invitedUser = setUpInvitedUser();
        Set<InvitedUser> updatedInvitedUsers = new HashSet<>(project.getAssignedInvitedUsers());
        updatedInvitedUsers.add(invitedUser);

        project.setAssignedInvitedUsers(updatedInvitedUsers);
        projectRepository.save(project);

        // when
        service.unassignUser(project, invitedUser);

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedInvitedUsers()).isEmpty();
        assertThat(project.getAssignedRegisteredUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedRegisteredUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(REGISTERED_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_DeleteDiagramFromProject_When_DiagramExists() throws IOException {

        // given
        Project project = setUpProject();
        Diagram diagram = Diagram.builder()
            .fileName("diagram.svg")
            .fileType(FileType.SVG)
            .project(project)
            .build();
        diagramRepository.save(diagram);
        FileIOUtils.saveFile(Path.of(diagramsLocation, project.getId().toString(), diagram.getFileName()), new ByteArrayInputStream("test".getBytes(
            StandardCharsets.UTF_8)));

        // when
        service.deleteDiagram(project, diagram);

        // then
        assertThat(project.getDiagrams()).isEmpty();
    }

    private RegisteredUser setUpRegisteredUser() {
        RegisteredUser registeredUser = RegisteredUser.builder()
            .fullName(REGISTERED_USER_FULL_NAME + " (2)")
            .emailAddress(REGISTERED_USER_MAIL_ADDRESS + " (2)")
            .assignedProjects(new HashSet<>())
            .build();

        return registeredUserRepository.save(registeredUser);
    }

    private InvitedUser setUpInvitedUser() {
        InvitedUser invitedUser = InvitedUser.builder()
            .fullName(INVITED_USER_FULL_NAME)
            .emailAddress(INVITED_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        return invitedUserRepository.save(invitedUser);
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .diagrams(new HashSet<>())
            .apiUpload(false)
            .assignedRegisteredUsers(new HashSet<>(Set.of(registeredUser)))
            .assignedInvitedUsers(new HashSet<>())
            .creator(registeredUser)
            .build();

        return projectRepository.save(project);
    }
}