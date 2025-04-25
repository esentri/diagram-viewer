package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.configuration.TestContainersInitializer;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@DirtiesContext
@ExtendWith(TestContainersInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = TestContainersInitializer.class)
class DiagramService_ITest {

    private static final String TEST_USER_MAIL_ADDRESS = "test-user@gmail.com";
    private static final String TEST_USER_FULL_NAME = "Max Mustermann";

    @Autowired
    DiagramService service;

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
            .build();

        registeredUserRepository.save(registeredUser);
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        registeredUserRepository.deleteAll();
        invitedUserRepository.deleteAll();
    }

    private Project setUpProject() {
        Project project = Project.builder()
            .name("project-1.0.0.jar")
            .boundedContextPackages(List.of("io.esentri.domain"))
            .apiUpload(false)
            .assignedRegisteredUsers(new ArrayList<>(List.of(registeredUser)))
            .creator(registeredUser)
            .build();

        return projectRepository.save(project);
    }
}