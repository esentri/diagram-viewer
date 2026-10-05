package io.domainlifecycles.diagramviewer.service.projectdomainmirror;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.scenario.RezeptionScenario;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorServiceImpl;
import io.domainlifecycles.diagramviewer.service.RegenerateDiagramsJobService;
import io.domainlifecycles.diagramviewer.util.CompressedJson;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.jackson3.JacksonDomainSerializer;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.serialize.jackson3.JacksonDomainCallsSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectDomainMirrorServiceTest {

    private static final JacksonDomainSerializer DOMAIN_SERIALIZER = new JacksonDomainSerializer(false);

    @Mock
    RegenerateDiagramsJobService regenerateDiagramsJobService;

    @Mock
    ProjectDomainMirrorRepository repository;

    ProjectDomainMirrorService projectDomainMirrorService;

    @BeforeEach
    void setUp() {
        projectDomainMirrorService = new ProjectDomainMirrorServiceImpl(regenerateDiagramsJobService, repository,
            DOMAIN_SERIALIZER, new JacksonDomainCallsSerializer(false));
    }

    @Test
    void Should_ReturnProjectDomainMirror_When_DomainMirrorExists() {

        // given
        ProjectDomainMirror projectDomainMirror = mock(ProjectDomainMirror.class);
        UUID projectId = new UUID(0, 0);

        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(projectDomainMirror));

        // when
        ProjectDomainMirror result = projectDomainMirrorService.getByProjectId(projectId);

        // then
        assertThat(result).isEqualTo(projectDomainMirror);
        verify(repository, times(1)).findByProjectId(projectId);
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnGetByProjectId_When_DomainMirrorDoesNotExist() {

        // given
        when(repository.findByProjectId(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> projectDomainMirrorService.getByProjectId(new UUID(0, 0)))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("No DomainTypeMirror found for project with id ");

        verify(repository, times(1)).findByProjectId(any());
    }

    @Test
    void Should_ReadDomainMirrorFromCompressedStorage() {

        // given
        UUID projectId = UUID.randomUUID();
        when(repository.findDomainMirrorGzByProjectId(projectId)).thenReturn(Optional.of(compress(RezeptionScenario.domainMirrorJson())));

        // when
        DomainMirror result = projectDomainMirrorService.getDomainMirror(projectId);

        // then
        assertThat(result.getDomainTypeMirror(RezeptionScenario.BUCHUNG_AGGREGATE)).isPresent();
        verify(repository, never()).findLegacyDomainMirrorByProjectId(any());
    }

    @Test
    void Should_ReadDomainMirrorFromLegacyStorage_When_ProjectWasUploadedBeforeCompression() {

        // given
        UUID projectId = UUID.randomUUID();
        DomainMirror legacyMirror = mock(DomainMirror.class);
        when(repository.findDomainMirrorGzByProjectId(projectId)).thenReturn(Optional.empty());
        when(repository.findLegacyDomainMirrorByProjectId(projectId)).thenReturn(Optional.of(legacyMirror));

        // when
        DomainMirror result = projectDomainMirrorService.getDomainMirror(projectId);

        // then
        assertThat(result).isSameAs(legacyMirror);
    }

    @Test
    void Should_ThrowDiagramViewerException_When_ProjectHasNoDomainMirror() {

        // given
        UUID projectId = UUID.randomUUID();
        when(repository.findDomainMirrorGzByProjectId(projectId)).thenReturn(Optional.empty());
        when(repository.findLegacyDomainMirrorByProjectId(projectId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> projectDomainMirrorService.getDomainMirror(projectId))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("No DomainMirror found");
    }

    @Test
    void Should_LoadDomainCallsFromCompressedStorage() {

        // given
        UUID projectId = UUID.randomUUID();
        DomainMirror mirror = DOMAIN_SERIALIZER.deserialize(RezeptionScenario.domainMirrorJson());
        when(repository.findDomainCallsGzByProjectId(projectId)).thenReturn(Optional.of(compress(RezeptionScenario.domainCallsJson())));

        // when
        Optional<DomainCalls> result = projectDomainMirrorService.loadDomainCalls(projectId, mirror);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().callers()).isNotEmpty();
        verify(repository, never()).findLegacyDomainCallsJsonByProjectId(any());
    }

    @Test
    void Should_LoadDomainCallsFromLegacyStorage_When_ProjectWasUploadedBeforeCompression() {

        // given
        UUID projectId = UUID.randomUUID();
        DomainMirror mirror = DOMAIN_SERIALIZER.deserialize(RezeptionScenario.domainMirrorJson());
        when(repository.findDomainCallsGzByProjectId(projectId)).thenReturn(Optional.empty());
        when(repository.findLegacyDomainCallsJsonByProjectId(projectId)).thenReturn(Optional.of(RezeptionScenario.domainCallsJson()));

        // when
        Optional<DomainCalls> result = projectDomainMirrorService.loadDomainCalls(projectId, mirror);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().callers()).isNotEmpty();
    }

    @Test
    void Should_ReturnNoDomainCalls_When_NoneWereUploaded() {

        // given
        UUID projectId = UUID.randomUUID();
        when(repository.findDomainCallsGzByProjectId(projectId)).thenReturn(Optional.empty());
        when(repository.findLegacyDomainCallsJsonByProjectId(projectId)).thenReturn(Optional.empty());

        // when / then
        assertThat(projectDomainMirrorService.loadDomainCalls(projectId, mock(DomainMirror.class))).isEmpty();
    }

    @Test
    void Should_StoreFileUploadCompressed_And_ReturnCreatedMirror() throws IOException {

        // given
        UUID projectId = UUID.randomUUID();
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        when(repository.existsByProjectId(projectId)).thenReturn(true);

        DomainMirror createdMirror = DOMAIN_SERIALIZER.deserialize(RezeptionScenario.domainMirrorJson());
        Set<String> domainModelPackages = Set.of("testPackage");
        Path path = mock(Path.class);
        UploadFileType uploadFileType = UploadFileType.JSON;

        try (MockedStatic<DomainModelUtils> domainModelUtils = Mockito.mockStatic(DomainModelUtils.class)) {
            domainModelUtils.when(() -> DomainModelUtils.initializeDomainMirrorFromFile(any(), any(), any())).thenReturn(createdMirror);

            // when
            DomainMirror result = projectDomainMirrorService.createOrUpdate(project, domainModelPackages, path, uploadFileType);

            // then: the created mirror is returned and stored compressed, readable back
            assertThat(result).isSameAs(createdMirror);
            ArgumentCaptor<byte[]> stored = ArgumentCaptor.forClass(byte[].class);
            verify(repository).updateCompressed(eq(projectId), stored.capture(), isNull());
            verify(regenerateDiagramsJobService).create(project);
            try (InputStream json = CompressedJson.decompress(stored.getValue())) {
                assertThat(DOMAIN_SERIALIZER.deserialize(json).getDomainTypeMirror(RezeptionScenario.BUCHUNG_AGGREGATE)).isPresent();
            }
        }
    }

    @Test
    void Should_InsertCompressed_When_ProjectHasNoDomainModelYet() {

        // given
        UUID projectId = UUID.randomUUID();
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        when(repository.existsByProjectId(projectId)).thenReturn(false);
        byte[] mirrorGz = compress("{}");

        // when
        projectDomainMirrorService.createOrUpdateCompressed(project, mirrorGz, null);

        // then: stored as given, nothing previous is loaded, no regeneration needed
        verify(repository).insertCompressed(any(UUID.class), eq(projectId), eq(mirrorGz), isNull());
        verify(repository, never()).findByProjectId(any());
        verify(repository, never()).updateCompressed(any(), any(), any());
        verifyNoInteractions(regenerateDiagramsJobService);
    }

    @Test
    void Should_UpdateCompressedWithoutLoadingPreviousModel_And_ScheduleRegeneration_When_ProjectHasDomainModel() {

        // given
        UUID projectId = UUID.randomUUID();
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        when(repository.existsByProjectId(projectId)).thenReturn(true);
        byte[] mirrorGz = compress("{}");
        byte[] callsGz = compress("{\"callsByCaller\":[]}");

        // when
        projectDomainMirrorService.createOrUpdateCompressed(project, mirrorGz, callsGz);

        // then
        verify(repository).updateCompressed(projectId, mirrorGz, callsGz);
        verify(repository, never()).findByProjectId(any());
        verify(repository, never()).insertCompressed(any(), any(), any(), any());
        verify(regenerateDiagramsJobService).create(project);
    }

    @Test
    void Should_DeleteWithoutLoadingTheModel() {

        // given
        UUID projectId = UUID.randomUUID();

        // when
        projectDomainMirrorService.delete(projectId);

        // then
        verify(repository).deleteByProjectIdWithoutLoading(projectId);
        verify(repository, never()).findByProjectId(any());
    }

    private static byte[] compress(String json) {
        return CompressedJson.compress(out -> {
            try {
                out.write(json.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        });
    }
}
