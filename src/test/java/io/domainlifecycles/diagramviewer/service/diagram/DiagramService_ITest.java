package io.domainlifecycles.diagramviewer.service.diagram;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DiagramService_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_MAIL_ADDRESS = "test-user@gmail.com";
    private static final String TEST_USER_FULL_NAME = "Max Mustermann";

    @Autowired
    DiagramService service;

    @Autowired
    SessionStorage sessionStorage;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    RegisteredUserRepository registeredUserRepository;

    @Autowired
    InvitedUserRepository invitedUserRepository;

    @Autowired
    DiagramRepository diagramRepository;

    private RegisteredUser registeredUser;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        registeredUser = RegisteredUser.builder()
            .fullName(TEST_USER_FULL_NAME)
            .emailAddress(TEST_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        registeredUserRepository.save(registeredUser);
    }

    @Test
    void Should_CreateDiagram() throws IOException {

        // given
        Project project = setUpProjectAndDomainMirror();
        String diagramName = "test-diagram";

        // when
        service.create(project, diagramName, new DomainModelVisibility(),
            new DiagramStylingConfiguration());

        // then
        assertThat(diagramRepository.findByName(diagramName)).isNotNull();
    }

    @AfterEach
    void tearDown() {
        sessionStorage.delete(projectId);
        projectRepository.deleteAll();
        diagramRepository.deleteAll();
        registeredUserRepository.deleteAll();
        invitedUserRepository.deleteAll();
    }

    private Project setUpProjectAndDomainMirror() throws IOException {
        Path path = saveDomainMirror();

        Project project = Project.builder()
            .name("test-project")
            .assignedRegisteredUsers(new HashSet<>(Set.of(registeredUser)))
            .assignedInvitedUsers(new HashSet<>())
            .diagrams(new HashSet<>())
            .creator(registeredUser)
            .build();

        Project persistedProject = projectRepository.save(project);
        projectId = persistedProject.getId();

        Set<String> domainModelPackages = new HashSet<>();
        domainModelPackages.add("com.esentri");

        sessionStorage.createOrUpdate(persistedProject, domainModelPackages, path, UploadFileType.JSON);

        return persistedProject;
    }

    private Path saveDomainMirror() throws IOException {
        byte[] jsonMirrorFileContents = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("mirror.json"))
            .readAllBytes();
        return FileIOUtils.saveTemporaryFile("test-mirror.json", jsonMirrorFileContents);
    }
}