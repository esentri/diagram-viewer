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
import java.util.ArrayList;
import java.util.HashSet;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ExtendWith(TestContainersInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = TestContainersInitializer.class)
class ProjectService_ITest {

    private static final String TEST_USER_MAIL_ADDRESS = "test-user@gmail.com";
    private static final String TEST_USER_FULL_NAME = "Max Mustermann";

    private static final String TEMPORARY_USER_FULL_NAME = "Temporary Mustermann";
    private static final String TEMPORARY_USER_MAIL_ADDRESS = "temporary@gmail.com";

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
        Project project = service.save(authenticatedUser, "/tmp/diagram-viewer",
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "test-project-1.0.0-ÄÖÜ.txt", "com.esentri");

        // then
        assertThat(project).isNotNull();
        assertThat(project.getProjectNameFull()).isEqualTo("test-project-1.0.0-ÄÖÜ.txt");
        assertThat(project.getProjectNameClean()).isEqualTo("test_project_1_0_0_ÄÖÜ_txt");
        assertThat(project.getDisplayName()).isEqualTo("test-project-1.0.0-ÄÖÜ.txt");
        assertThat(project.getCreator()).isEqualTo(authenticatedUser);
        assertThat(project.getAssignedAuthenticatedUsers().size()).isEqualTo(1);
        assertThat(project.getAssignedAuthenticatedUsers()).contains(authenticatedUser);
        assertThat(project.getCreator()).isEqualTo(authenticatedUser);
        assertThat(project.getAssignedTemporaryUsers()).isEmpty();
        assertThat(project.getAbsolutePathToTarget()).isNotBlank();
        assertThat(project.getBoundedContextPackages().size()).isEqualTo(1);
        assertThat(project.getBoundedContextPackages().get(0)).isEqualTo("com.esentri");

        FileIOUtils.deleteFileByAbsolutePath("/tmp/diagram-viewer/test-project-1.0.0-ÄÖÜ.txt");
    }

    @Test
    void Should_AssignUserToProject_When_UserIsTemporary() {

        // given
        Project project = setUpProject();

        // when
        service.assignUser(project, TEMPORARY_USER_MAIL_ADDRESS);

        // then
        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(updatedProject).isNotNull();
        assertThat(updatedProject.getAssignedTemporaryUsers())
            .anySatisfy(temporaryUser -> assertThat(temporaryUser.getEmailAddress())
                .isEqualTo(TEMPORARY_USER_MAIL_ADDRESS));
        assertThat(updatedProject.getAssignedAuthenticatedUsers())
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
        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(updatedProject).isNotNull();
        assertThat(updatedProject.getAssignedTemporaryUsers()).isEmpty();
        assertThat(updatedProject.getAssignedAuthenticatedUsers().size()).isEqualTo(2);
        assertThat(updatedProject.getAssignedAuthenticatedUsers())
            .anySatisfy(authenticatedUser -> assertThat(authenticatedUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
        assertThat(updatedProject.getAssignedAuthenticatedUsers())
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
        Project updatedProject = service.unassignUser(project, anotherAuthenticatedUser);

        // then
        assertThat(updatedProject).isNotNull();
        assertThat(updatedProject.getAssignedTemporaryUsers()).isEmpty();
        assertThat(updatedProject.getAssignedAuthenticatedUsers().size()).isEqualTo(1);
        assertThat(updatedProject.getAssignedAuthenticatedUsers())
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
        Project updatedProject = service.unassignUser(project, temporaryUser);

        // then
        assertThat(updatedProject).isNotNull();
        assertThat(updatedProject.getAssignedTemporaryUsers()).isEmpty();
        assertThat(updatedProject.getAssignedAuthenticatedUsers().size()).isEqualTo(1);
        assertThat(updatedProject.getAssignedAuthenticatedUsers())
            .anySatisfy(authenticatedUser -> assertThat(authenticatedUser.getEmailAddress())
                .isEqualTo(TEST_USER_MAIL_ADDRESS));
    }

    @Test
    void Should_DeleteDiagramFromProject_When_DiagramExists() throws IOException {

        // given
        Project project = setUpProject();
        Diagram diagram = Diagram.builder()
            .fileName("diagram")
            .fileType(FileType.SVG)
            .fullAbsoluteLocationPath("/tmp/diagram-viewer/diagram.svg")
            .project(project)
            .build();
        diagramRepository.save(diagram);
        FileIOUtils.saveFile("/tmp/diagram-viewer/diagram.svg", new ByteArrayInputStream("test".getBytes(
            StandardCharsets.UTF_8)));

        // when
        Project updatedProject = service.deleteDiagram(project, diagram);

        // then
        assertThat(updatedProject).isNotNull();
        assertThat(updatedProject.getDiagrams()).isEmpty();
    }

    private AuthenticatedUser setUpAuthenticatedUser() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
            .fullName("Moritz Mustermann")
            .emailAddress("moritz.mustermann@gmail.com")
            .build();

        return authenticatedUserRepository.save(authenticatedUser);
    }

    private TemporaryUser setUpTemporaryUser() {
        TemporaryUser temporaryUser = TemporaryUser.builder()
            .fullName("Moritz Mustermann")
            .emailAddress("moritz.mustermann@gmail.com")
            .build();

        return temporaryUserRepository.save(temporaryUser);
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .projectNameClean("project_1_0_0_jar")
            .projectNameFull("project-1.0.0.jar")
            .displayName("project-1.0.0.jar")
            .absolutePathToTarget("target/project-1.0.0.jar")
            .boundedContextPackages(List.of("io.esentri.domain"))
            .assignedAuthenticatedUsers(List.of(authenticatedUser))
            .creator(authenticatedUser)
            .build();

        return projectRepository.save(project);
    }
}