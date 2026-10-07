package io.domainlifecycles.diagramviewer.service.projectdomainmirror;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.scenario.RezeptionScenario;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.util.CompressedJson;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.jackson3.JacksonDomainSerializer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the storage of a project's domain model against a real PostgreSQL:
 * compressed storage, and the fallback to the legacy uncompressed columns for projects last uploaded
 * before the compressed storage was introduced.
 */
@SpringBootTest(properties = "regenerateDiagramsTask.rate=3600000")
class ProjectDomainMirrorStorage_ITest extends BaseIntegrationTest {

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectDomainMirrorRepository projectDomainMirrorRepository;

    @Autowired
    ProjectDomainMirrorService projectDomainMirrorService;

    private Project project;

    @BeforeEach
    void setUp() {
        AppUser appUser = appUserRepository.save(AppUser.builder()
            .firstName("Storage")
            .lastName("Tester")
            .emailAddress("storage-tester@gmail.com")
            .status(UserStatus.ACTIVE)
            .build());
        project = projectRepository.save(Project.builder()
            .name("storage_project")
            .diagrams(new HashSet<>())
            .assignedUsers(new HashSet<>(Set.of(appUser)))
            .creator(appUser)
            .build());
    }

    @AfterEach
    void tearDown() {
        projectDomainMirrorRepository.deleteAll();
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void Should_ReadLegacyUncompressedModel_And_SwitchToCompressedStorageOnNextUpload() {

        // given: a project stored the way it was before the compressed storage was introduced
        DomainMirror legacyMirror = new JacksonDomainSerializer(false).deserialize(RezeptionScenario.domainMirrorJson());
        projectDomainMirrorRepository.save(ProjectDomainMirror.builder()
            .projectId(project.getId())
            .domainMirror(legacyMirror)
            .domainCalls(RezeptionScenario.domainCallsJson())
            .build());

        // then: it is still readable
        DomainMirror readLegacy = projectDomainMirrorService.getDomainMirror(project.getId());
        assertThat(readLegacy.getDomainTypeMirror(RezeptionScenario.BUCHUNG_AGGREGATE)).isPresent();
        assertThat(projectDomainMirrorService.hasDomainCalls(project.getId())).isTrue();
        assertThat(projectDomainMirrorService.loadDomainCalls(project.getId(), readLegacy)).isPresent();

        // when: it is uploaded again (without a static analysis result this time)
        projectDomainMirrorService.createOrUpdateCompressed(project, compress(RezeptionScenario.domainMirrorJson()), null);

        // then: it is stored compressed only, the legacy columns are cleared
        ProjectDomainMirror stored = projectDomainMirrorRepository.findByProjectId(project.getId()).orElseThrow();
        assertThat(stored.getDomainMirrorGz()).isNotEmpty();
        assertThat(stored.getDomainMirror()).isNull();
        assertThat(stored.getDomainCalls()).isNull();
        assertThat(stored.getDomainCallsGz()).isNull();
        assertThat(projectDomainMirrorService.getDomainMirror(project.getId())
            .getDomainTypeMirror(RezeptionScenario.BUCHUNG_AGGREGATE)).isPresent();
        assertThat(projectDomainMirrorService.hasDomainCalls(project.getId())).isFalse();
    }

    @Test
    void Should_StoreAndReadCompressedModel_And_DeleteIt() {

        // given
        projectDomainMirrorService.createOrUpdateCompressed(project,
            compress(RezeptionScenario.domainMirrorJson()), compress(RezeptionScenario.domainCallsJson()));

        // when
        DomainMirror mirror = projectDomainMirrorService.getDomainMirror(project.getId());

        // then
        assertThat(mirror.getDomainTypeMirror(RezeptionScenario.ZIMMER_AGGREGATE)).isPresent();
        assertThat(projectDomainMirrorService.hasDomainCalls(project.getId())).isTrue();
        assertThat(projectDomainMirrorService.loadDomainCalls(project.getId(), mirror).orElseThrow().callers()).isNotEmpty();

        // when
        projectDomainMirrorService.delete(project.getId());

        // then
        assertThat(projectDomainMirrorRepository.existsByProjectId(project.getId())).isFalse();
    }

    private static byte[] compress(String json) {
        return CompressedJson.compress(out -> {
            try {
                out.write(json.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
