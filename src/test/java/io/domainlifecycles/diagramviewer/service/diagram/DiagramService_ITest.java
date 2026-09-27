package io.domainlifecycles.diagramviewer.service.diagram;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
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
    private static final String TEST_USER_FIRST_NAME = "Max";
    private static final String TEST_USER_LAST_NAME = "Mustermann";

    @Autowired
    DiagramService service;

    @Autowired
    SessionStorage sessionStorage;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    DiagramRepository diagramRepository;

    private AppUser appUser;
    private UUID projectId;

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

    @Test
    void Should_CreateDiagram() throws IOException {

        // given
        Project project = setUpProjectAndDomainMirror();
        String diagramName = "test-diagram";

        // when
        service.create(project, diagramName, new DomainModelVisibility(),
            new DiagramStylingConfiguration());

        // then
        assertThat(diagramRepository.findByProjectIdAndName(project.getId(), diagramName)).isPresent();
    }

    @AfterEach
    void tearDown() {
        sessionStorage.delete(projectId);
        projectRepository.deleteAll();
        diagramRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    private Project setUpProjectAndDomainMirror() throws IOException {
        Path path = saveDomainMirror();

        Project project = Project.builder()
            .name("test-project")
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .diagrams(new HashSet<>())
            .creator(appUser)
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