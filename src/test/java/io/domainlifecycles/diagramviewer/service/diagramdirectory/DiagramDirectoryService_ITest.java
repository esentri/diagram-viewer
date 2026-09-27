package io.domainlifecycles.diagramviewer.service.diagramdirectory;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.repository.DiagramDirectoryRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
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
    private static final String TEST_USER_FIRST_NAME = "Max";
    private static final String TEST_USER_LAST_NAME = "Mustermann";

    @Autowired
    DiagramDirectoryService service;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    DiagramRepository diagramRepository;

    @Autowired
    DiagramDirectoryRepository diagramDirectoryRepository;

    @Autowired
    ProjectService projectService;

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
    void Should_CreateDiagramDirectory_When_AllValuesAreValid() {
        // given
        Project project = setUpProject();
        Diagram firstDiagram = Diagram.builder()
            .name("first")
            .project(project)
            .build();
        Diagram secondDiagram = Diagram.builder()
            .name("second")
            .project(project)
            .build();

        diagramRepository.save(firstDiagram);
        diagramRepository.save(secondDiagram);

        String directoryName = "First Directory";

        // when
        service.create(directoryName, project, Set.of(firstDiagram, secondDiagram));

        // then
        DiagramDirectory diagramDirectory = project.getTopLevelDiagramDirectories().stream()
            .filter(directory -> directoryName.equals(directory.getName()))
            .findFirst().orElseThrow();
        assertThat(service.getById(diagramDirectory.getId())).isNotNull();
    }

    @Test
    void Should_PersistNestedDirectories_And_DeleteThemWithTheirParent() {

        // given: two context folders with a "Commands" folder each, one holding a diagram
        Project project = setUpProject();
        project.setDiagrams(new HashSet<>());
        DiagramDirectory buchung = service.findOrCreate(project, null, "Buchung");
        DiagramDirectory buchungCommands = service.findOrCreate(project, buchung, "Commands");
        DiagramDirectory zimmer = service.findOrCreate(project, null, "Zimmer");
        DiagramDirectory zimmerCommands = service.findOrCreate(project, zimmer, "Commands");
        Diagram diagram = diagramRepository.save(Diagram.builder().name("Buchung - CheckeGastAus").project(project).build());
        project.addDiagram(diagram);
        service.add(buchungCommands, diagram);

        // then: same names below different parents are different folders, found again on a second run
        assertThat(buchungCommands).isNotEqualTo(zimmerCommands);
        assertThat(service.findOrCreate(project, buchung, "Commands")).isEqualTo(buchungCommands);
        assertThat(service.getById(buchungCommands.getId()).getParent()).isEqualTo(buchung);

        // when: the context folder is deleted
        projectService.deleteDiagramDirectory(project, buchung);

        // then: its sub folder went with it, the other context stays, the diagram is kept without folder
        assertThat(diagramDirectoryRepository.findById(buchung.getId())).isEmpty();
        assertThat(diagramDirectoryRepository.findById(buchungCommands.getId())).isEmpty();
        assertThat(diagramDirectoryRepository.findById(zimmerCommands.getId())).isPresent();
        assertThat(diagramRepository.findById(diagram.getId()).orElseThrow().getDiagramDirectory()).isNull();
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .diagramDirectories(new HashSet<>())
            .creator(appUser)
            .build();

        return projectRepository.save(project);
    }

}