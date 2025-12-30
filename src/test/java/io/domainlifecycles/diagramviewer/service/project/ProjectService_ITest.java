package io.domainlifecycles.diagramviewer.service.project;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.InvitedUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectService_ITest extends BaseIntegrationTest {

    private static final String REGISTERED_USER_FULL_NAME = "Max Mustermann";
    private static final String REGISTERED_USER_MAIL_ADDRESS = "max.mustermann@gmail.com";

    private static final String INVITED_USER_FULL_NAME = "Moritz Mustermann";
    private static final String INVITED_USER_MAIL_ADDRESS = "moritz.mustermann@gmail.com";

    @Autowired
    ProjectService service;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    InvitedUserRepository invitedUserRepository;

    @Autowired
    DiagramRepository diagramRepository;

    @Value("${diagrams.location}") String diagramsLocation;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = AppUser.builder()
            .fullName(REGISTERED_USER_FULL_NAME)
            .emailAddress(REGISTERED_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        appUserRepository.save(appUser);
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
        invitedUserRepository.deleteAll();
    }

    @Test
    void Should_CreateProjectFromJsonDomainMirror_When_AllValuesAreValid() throws IOException {
        // given
        Set<String> domainModelPackages = new HashSet<>();
        domainModelPackages.add("com.esentri");

        String projectName = "testProject";

        byte[] jsonMirrorFileContents = getClass().getClassLoader()
            .getResourceAsStream("mirror.json")
            .readAllBytes();

        Path path = FileIOUtils.saveTemporaryFile("test-mirror.json", jsonMirrorFileContents);

        // when
        Project project = service.create(projectName, domainModelPackages, appUser, path, UploadFileType.JSON);

        // then
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo(projectName);
        assertThat(project.getCreator().getId()).isEqualTo(appUser.getId());
        assertThat(project.getAssignedUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedUsers().stream().findFirst().orElseThrow().getId()).isEqualTo(appUser.getId());
        assertThat(project.getCreator().getId()).isEqualTo(appUser.getId());
        assertThat(project.getAssignedInvitedUsers()).isEmpty();
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
        assertThat(project.getAssignedUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(REGISTERED_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_AssignUserToProject_When_UserIsRegistered() {

        // given
        Project project = setUpProject();
        AppUser anotherAppUser = setUpRegisteredUser();

        // when
        service.assignUser(project, anotherAppUser.getEmailAddress());

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedInvitedUsers()).isEmpty();
        assertThat(project.getAssignedUsers().size()).isEqualTo(2);
        assertThat(project.getAssignedUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(REGISTERED_USER_MAIL_ADDRESS));
        assertThat(project.getAssignedUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(anotherAppUser.getEmailAddress()));
    }

    @Test
    void Should_UnassignUserFromProject_When_UserIsRegistered() {

        // given
        Project project = setUpProject();
        AppUser anotherAppUser = setUpRegisteredUser();
        Set<AppUser> updatedAppUsers = new HashSet<>(project.getAssignedUsers());
        updatedAppUsers.add(anotherAppUser);

        project.setAssignedUsers(updatedAppUsers);
        projectRepository.save(project);

        // when
        service.unassignUser(project, anotherAppUser);

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedInvitedUsers()).isEmpty();
        assertThat(project.getAssignedUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedUsers())
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
        assertThat(project.getAssignedUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(REGISTERED_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_DeleteDiagramFromProject_When_DiagramExists() throws IOException {

        // given
        Project project = setUpProject();
        Diagram diagram = Diagram.builder()
            .name("diagram")
            .project(project)
            .build();
        diagramRepository.save(diagram);
        FileIOUtils.saveFile(Path.of(diagramsLocation, project.getId().toString(), diagram.getName()), new ByteArrayInputStream("test".getBytes(
            StandardCharsets.UTF_8)));

        // when
        service.deleteDiagram(project, diagram);

        // then
        assertThat(project.getDiagrams()).isEmpty();
    }

    private AppUser setUpRegisteredUser() {
        AppUser appUser = AppUser.builder()
            .fullName(REGISTERED_USER_FULL_NAME + " (2)")
            .emailAddress(REGISTERED_USER_MAIL_ADDRESS + " (2)")
            .assignedProjects(new HashSet<>())
            .build();

        return appUserRepository.save(appUser);
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
            .assignedAppUsers(new HashSet<>(Set.of(appUser)))
            .assignedInvitedUsers(new HashSet<>())
            .creator(appUser)
            .build();

        return projectRepository.save(project);
    }
}