package io.domainlifecycles.diagramviewer.service.registereduser;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import io.domainlifecycles.diagramviewer.service.RegisteredUserService;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RegisteredUserService_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_MAIL_ADDRESS = "test-user@gmail.com";
    private static final String TEST_USER_FULL_NAME = "Max Mustermann";

    @Autowired
    private RegisteredUserService service;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @BeforeEach
    void setUp() {
        RegisteredUser registeredUser = RegisteredUser.builder()
            .fullName(TEST_USER_FULL_NAME)
            .emailAddress(TEST_USER_MAIL_ADDRESS)
            .assignedProjects(new HashSet<>())
            .build();

        registeredUserRepository.save(registeredUser);
    }

    @AfterEach
    void tearDown() {
        registeredUserRepository.deleteAll();
    }

    @Test
    void Should_KnowUser_When_UserIsInDatabase() {

        // when
        boolean result = service.userKnown(TEST_USER_MAIL_ADDRESS);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void Should_NotKnowUser_When_NoSuchUserIsInDatabase() {

        // when
        boolean result = service.userKnown("unknown@gmail.com");

        // then
        assertThat(result).isFalse();
    }
}