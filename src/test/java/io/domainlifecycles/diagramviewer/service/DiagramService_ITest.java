package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.configuration.TestContainersInitializer;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.repository.AuthenticatedUserRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.repository.TemporaryUserRepository;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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