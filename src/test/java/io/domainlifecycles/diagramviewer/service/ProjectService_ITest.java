package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.configuration.TestContainersInitializer;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.repository.AuthenticatedUserRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.TemporaryUserRepository;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    private static final String TEST_USER_FULL_NAME = "Max Mustermann";
    private static final String TEST_USER_MAIL_ADDRESS = "max.mustermann@gmail.com";

    private static final String TEMPORARY_USER_FULL_NAME = "Moritz Mustermann";
    private static final String TEMPORARY_USER_MAIL_ADDRESS = "moritz.mustermann@gmail.com";

    @Autowired
    ProjectService service;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AuthenticatedUserRepository authenticatedUserRepository;

    @Autowired
    TemporaryUserRepository temporaryUserRepository;

    @Autowired
    DiagramRepository diagramRepository;

    @Value("${targets.location}") String targetsDirectory;

    @Value("${diagrams.location}") String diagramsLocation;

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
    void Should_CreateProject_When_AllValuesAreValid() throws IOException {

        // when
        Project project = service.save(authenticatedUser,
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "test-project-1.0.0-ÄÖÜ.txt", "com.esentri");

        // then
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo("test_project_1_0_0_ÄÖÜ_txt");
        assertThat(project.getCreator().getId()).isEqualTo(authenticatedUser.getId());
        assertThat(project.getAssignedAuthenticatedUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedAuthenticatedUsers().get(0).getId()).isEqualTo(authenticatedUser.getId());
        assertThat(project.getCreator().getId()).isEqualTo(authenticatedUser.getId());
        assertThat(project.getAssignedTemporaryUsers()).isEmpty();
        assertThat(project.getBoundedContextPackages().size()).isEqualTo(1);
        assertThat(project.getBoundedContextPackages().get(0)).isEqualTo("com.esentri");

        FileIOUtils.deleteDirectoryRecursively(Path.of(targetsDirectory));
    }

    @Test
    void Should_AssignUserToProject_When_UserIsTemporary() {

        // given
        Project project = setUpProject();

        // when
        service.assignUser(project, TEMPORARY_USER_MAIL_ADDRESS);

        // then
        assertThat(project.getAssignedTemporaryUsers())
            .anySatisfy(temporaryUser -> assertThat(temporaryUser.getEmailAddress())
                .isEqualTo(TEMPORARY_USER_MAIL_ADDRESS));
        assertThat(project.getAssignedAuthenticatedUsers())
            .anySatisfy(authenticatedUser -> assertThat(authenticatedUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_AssignUserToProject_When_UserIsAuthenticated() {

        // given
        Project project = setUpProject();
        AuthenticatedUser anotherAuthenticatedUser = setUpAuthenticatedUser();

        // when
        service.assignUser(project, anotherAuthenticatedUser.getEmailAddress());

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedTemporaryUsers()).isEmpty();
        assertThat(project.getAssignedAuthenticatedUsers().size()).isEqualTo(2);
        assertThat(project.getAssignedAuthenticatedUsers())
            .anySatisfy(authenticatedUser -> assertThat(authenticatedUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
        assertThat(project.getAssignedAuthenticatedUsers())
            .anySatisfy(authenticatedUser -> assertThat(authenticatedUser.getEmailAddress())
                .isEqualTo(anotherAuthenticatedUser.getEmailAddress()));
    }

    @Test
    void Should_UnassignUserFromProject_When_UserIsAuthenticated() {

        // given
        Project project = setUpProject();
        AuthenticatedUser anotherAuthenticatedUser = setUpAuthenticatedUser();
        List<AuthenticatedUser> updatedAuthenticatedUsers = new ArrayList<>(project.getAssignedAuthenticatedUsers());
        updatedAuthenticatedUsers.add(anotherAuthenticatedUser);

        project.setAssignedAuthenticatedUsers(updatedAuthenticatedUsers);
        projectRepository.save(project);

        // when
        service.unassignUser(project, anotherAuthenticatedUser);

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedTemporaryUsers()).isEmpty();
        assertThat(project.getAssignedAuthenticatedUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedAuthenticatedUsers())
            .anySatisfy(authenticatedUser -> assertThat(authenticatedUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_UnassignUserFromProject_When_UserIsTemporary() {

        // given
        Project project = setUpProject();
        TemporaryUser temporaryUser = setUpTemporaryUser();
        List<TemporaryUser> updatedTemporaryUsers = new ArrayList<>(project.getAssignedTemporaryUsers());
        updatedTemporaryUsers.add(temporaryUser);

        project.setAssignedTemporaryUsers(updatedTemporaryUsers);
        projectRepository.save(project);

        // when
        service.unassignUser(project, temporaryUser);

        // then
        assertThat(project).isNotNull();
        assertThat(project.getAssignedTemporaryUsers()).isEmpty();
        assertThat(project.getAssignedAuthenticatedUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedAuthenticatedUsers())
            .anySatisfy(authenticatedUser -> assertThat(authenticatedUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
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

    private AuthenticatedUser setUpAuthenticatedUser() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
            .fullName(TEST_USER_FULL_NAME + " (2)")
            .emailAddress(TEST_USER_MAIL_ADDRESS + " (2)")
            .build();

        return authenticatedUserRepository.save(authenticatedUser);
    }

    private TemporaryUser setUpTemporaryUser() {
        TemporaryUser temporaryUser = TemporaryUser.builder()
            .fullName(TEMPORARY_USER_FULL_NAME)
            .emailAddress(TEMPORARY_USER_MAIL_ADDRESS)
            .build();

        return temporaryUserRepository.save(temporaryUser);
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
}