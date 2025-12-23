package io.domainlifecycles.diagramviewer.service.diagramdirectory;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DiagramDirectoryService_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_MAIL_ADDRESS = "test-user@gmail.com";
    private static final String TEST_USER_FULL_NAME = "Max Mustermann";

    @Autowired
    DiagramDirectoryService service;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    RegisteredUserRepository registeredUserRepository;

    @Autowired
    InvitedUserRepository invitedUserRepository;

    @Autowired
    DiagramRepository diagramRepository;

    private RegisteredUser registeredUser;

    @BeforeEach
    void setUp() {
        registeredUser = RegisteredUser.builder()
            .fullName(TEST_USER_FULL_NAME)
            .emailAddress(TEST_USER_MAIL_ADDRESS)
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
    void Should_CreateDiagramDirectory_When_AllValuesAreValid() {
        // given
        Project project = setUpProject();
        Diagram firstDiagram = Diagram.builder()
            .fileName("first.svg")
            .fileType(FileType.SVG)
            .project(project)
            .build();
        Diagram secondDiagram = Diagram.builder()
            .fileName("second.svg")
            .fileType(FileType.SVG)
            .project(project)
            .build();

        diagramRepository.save(firstDiagram);
        diagramRepository.save(secondDiagram);

        String directoryName = "First Directory";

        // when
        service.create(directoryName, project, Set.of(firstDiagram, secondDiagram));

        // then
        DiagramDirectory diagramDirectory = service.getByName(directoryName);
        assertThat(diagramDirectory).isNotNull();
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .assignedRegisteredUsers(new HashSet<>(Set.of(registeredUser)))
            .assignedInvitedUsers(new HashSet<>())
            .diagramDirectories(new HashSet<>())
            .creator(registeredUser)
            .build();

        return projectRepository.save(project);
    }

}