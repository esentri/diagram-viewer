package io.domainlifecycles.diagramviewer.service.appuser;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.service.AppUserService;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AppUserService_ITest extends BaseIntegrationTest {

    private static final String TEST_USER_MAIL_ADDRESS = "test-user@gmail.com";
    private static final String TEST_USER_FIRST_NAME = "Max";
    private static final String TEST_USER_LAST_NAME = "Mustermann";

    @Autowired
    private AppUserService service;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        AppUser appUser = AppUser.builder()
            .firstName(TEST_USER_FIRST_NAME)
            .lastName(TEST_USER_LAST_NAME)
            .emailAddress(TEST_USER_MAIL_ADDRESS)
            .status(UserStatus.ACTIVE)
            .build();

        appUserRepository.save(appUser);
    }

    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
    }

    @Test
    void Should_KnowUser_When_UserIsInDatabase() {

        // when
        boolean result = service.userKnownAndActive(TEST_USER_MAIL_ADDRESS);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void Should_NotKnowUser_When_NoSuchUserIsInDatabase() {

        // when
        boolean result = service.userKnownAndActive("unknown@gmail.com");

        // then
        assertThat(result).isFalse();
    }
}