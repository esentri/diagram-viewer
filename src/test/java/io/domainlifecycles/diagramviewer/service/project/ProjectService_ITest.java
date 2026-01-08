package io.domainlifecycles.diagramviewer.service.project;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
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
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProjectService_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_MAIL_ADDRESS = "test-user@gmail.com";
    private static final String TEST_USER_FIRST_NAME = "Max";
    private static final String TEST_USER_LAST_NAME = "Mustermann";

    @Autowired
    ProjectService service;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AppUserRepository appUserRepository;
    @Autowired
    DiagramRepository diagramRepository;

    @Value("${diagrams.location}") String diagramsLocation;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = AppUser.builder()
            .firstName(TEST_USER_FIRST_NAME)
            .lastName(TEST_USER_LAST_NAME)
            .emailAddress(TEST_USER_MAIL_ADDRESS)
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
    }

    @Test
    void Should_FindProjectAssignedToUser_When_ProjectWithUserAssignedExists() {

        // given
        Project project = setUpProject();

        // when
        List<Project> result = service.getAllAssignedSortedByCreationDate(appUser);

        // then
        assertThat(result.get(0)).isEqualTo(project);
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnAssignUser_When_UserIsAlreadyAssigned() {

        // given
        Project project = setUpProject();

        // when
        assertThatThrownBy(() -> service.assignUser(project, TEST_USER_MAIL_ADDRESS))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("User 'test-user@gmail.com' is already assigned to project.");

        // then
        assertThat(project.getAssignedUsers())
            .anySatisfy(invitedUser -> assertThat(invitedUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_AssignUserToProject_When_UserIsRegistered() {

        // given
        Project project = setUpProject();
        AppUser anotherAppUser = setUpAnotherUser();

        // when
        service.assignUser(project, anotherAppUser.getEmailAddress());

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedUsers().size()).isEqualTo(2);
        assertThat(project.getAssignedUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
        assertThat(project.getAssignedUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(anotherAppUser.getEmailAddress()));
    }

    @Test
    void Should_UnassignUserFromProject_When_UserIsRegistered() {

        // given
        Project project = setUpProject();
        AppUser anotherAppUser = setUpAnotherUser();
        Set<AppUser> updatedAppUsers = new HashSet<>(project.getAssignedUsers());
        updatedAppUsers.add(anotherAppUser);

        project.setAssignedUsers(updatedAppUsers);
        projectRepository.save(project);

        // when
        service.unassignUser(project, anotherAppUser);

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedUsers())
            .anySatisfy(registeredUser -> assertThat(registeredUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
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

    private AppUser setUpAnotherUser() {
        AppUser appUser = AppUser.builder()
            .firstName(TEST_USER_FIRST_NAME + " (2)")
            .lastName(TEST_USER_LAST_NAME + " (2)")
            .emailAddress(TEST_USER_MAIL_ADDRESS + " (2)")
            .status(UserStatus.ACTIVE)
            .build();

        return appUserRepository.save(appUser);
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
}