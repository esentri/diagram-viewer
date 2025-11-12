package io.domainlifecycles.diagramviewer.service.projectdomainmirror;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorServiceImpl;
import io.domainlifecycles.diagramviewer.service.RegenerateDiagramsJobService;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectDomainMirrorServiceTest {

    @Captor
    ArgumentCaptor<ProjectDomainMirror> projectDomainMirrorArgumentCaptor;

    @Mock
    RegenerateDiagramsJobService regenerateDiagramsJobService;

    @Mock
    ProjectDomainMirrorRepository repository;

    ProjectDomainMirrorService projectDomainMirrorService;

    @BeforeEach
    void setUp() {
        projectDomainMirrorService = new ProjectDomainMirrorServiceImpl(regenerateDiagramsJobService, repository);
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
    void Should_GetAllDomainTypeMirrorsWithoutEnumsAndIds() {

        // given
        JacksonDomainSerializer domainSerializerMock = mock(JacksonDomainSerializer.class);
        ReflectionTestUtils.setField(projectDomainMirrorService, "serializer", domainSerializerMock);

        String firstDomainTypeMirrorStringMock = "firstDomainTypeMirrorMock";
        String secondDomainTypeMirrorStringMock = "secondDomainTypeMirrorMock";

        when(repository.findProjectDomainTypesWithoutEnumsAndIds(any())).thenReturn(
            List.of(firstDomainTypeMirrorStringMock,
                secondDomainTypeMirrorStringMock));

        DomainTypeMirror firstDomainTypeMirrorMock = mock(DomainTypeMirror.class);
        DomainTypeMirror secondDomainTypeMirrorMock = mock(DomainTypeMirror.class);

        when(domainSerializerMock.deserializeTypeMirror(eq(firstDomainTypeMirrorStringMock))).thenReturn(
            firstDomainTypeMirrorMock);
        when(domainSerializerMock.deserializeTypeMirror(eq(secondDomainTypeMirrorStringMock))).thenReturn(
            secondDomainTypeMirrorMock);

        // when
        List<DomainTypeMirror> result = projectDomainMirrorService.getAllDomainTypeMirrorsWithoutEnumsAndIds(
            new UUID(0, 0));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(firstDomainTypeMirrorMock);
        assertThat(result.get(1)).isEqualTo(secondDomainTypeMirrorMock);

        verify(repository, times(1)).findProjectDomainTypesWithoutEnumsAndIds(any());
        verify(domainSerializerMock, times(1)).deserializeTypeMirror(firstDomainTypeMirrorStringMock);
        verify(domainSerializerMock, times(1)).deserializeTypeMirror(secondDomainTypeMirrorStringMock);
    }

    @Test
    void Should_GetAllAggregateRootMirrors() {

        // given
        JacksonDomainSerializer domainSerializerMock = mock(JacksonDomainSerializer.class);
        ReflectionTestUtils.setField(projectDomainMirrorService, "serializer", domainSerializerMock);

        String firstDomainTypeMirrorStringMock = "firstDomainTypeMirrorMock";
        String secondDomainTypeMirrorStringMock = "secondDomainTypeMirrorMock";

        when(repository.findProjectAggregateTypes(any())).thenReturn(
            List.of(firstDomainTypeMirrorStringMock,
                secondDomainTypeMirrorStringMock));

        AggregateRootMirror firstAggregateRootMirrorMock = mock(AggregateRootMirror.class);
        AggregateRootMirror secondAggregateRootMirrorMock = mock(AggregateRootMirror.class);

        when(domainSerializerMock.deserializeTypeMirror(eq(firstDomainTypeMirrorStringMock))).thenReturn(
            firstAggregateRootMirrorMock);
        when(domainSerializerMock.deserializeTypeMirror(eq(secondDomainTypeMirrorStringMock))).thenReturn(
            secondAggregateRootMirrorMock);

        // when
        List<AggregateRootMirror> result = projectDomainMirrorService.getAllAggregateRootMirrors(
            new UUID(0, 0));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(firstAggregateRootMirrorMock);
        assertThat(result.get(1)).isEqualTo(secondAggregateRootMirrorMock);

        verify(repository, times(1)).findProjectAggregateTypes(any());
        verify(domainSerializerMock, times(1)).deserializeTypeMirror(firstDomainTypeMirrorStringMock);
        verify(domainSerializerMock, times(1)).deserializeTypeMirror(secondDomainTypeMirrorStringMock);
    }

    @Test
    void Should_CreateOrUpdateWithoutDomainMirror() {

        // given
        UUID projectId = new UUID(0, 0);
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(projectId);

        Set<String> domainModelPackages = Set.of("testPackage");
        Path pathMock = mock(Path.class);
        UploadFileType uploadFileTypeMock = mock(UploadFileType.class);

        ProjectDomainMirror projectDomainMirrorMock = mock(ProjectDomainMirror.class);
        DomainMirror domainMirrorMock = mock(DomainMirror.class);

        when(repository.findByProjectId(eq(projectId))).thenReturn(Optional.of(projectDomainMirrorMock));
        when(repository.save(projectDomainMirrorMock)).thenReturn(projectDomainMirrorMock);
        doNothing().when(regenerateDiagramsJobService).create(eq(projectMock));

        try(MockedStatic<DomainModelUtils> domainModelUtilsMockedStatic = Mockito.mockStatic(DomainModelUtils.class)) {
            domainModelUtilsMockedStatic.when(() -> DomainModelUtils.initializeDomainMirrorFromFile(any(), any(), any())).thenReturn(domainMirrorMock);

            // when
            ProjectDomainMirror result = projectDomainMirrorService.createOrUpdate(projectMock, domainModelPackages, pathMock, uploadFileTypeMock);

            // then
            assertThat(result).isEqualTo(projectDomainMirrorMock);
            verify(repository, times(1)).findByProjectId(eq(projectId));
            verify(regenerateDiagramsJobService, times(1)).create(eq(projectMock));
            verify(repository, times(1)).save(projectDomainMirrorMock);
            domainModelUtilsMockedStatic.verify(() -> DomainModelUtils.initializeDomainMirrorFromFile(eq(pathMock), eq(domainModelPackages), eq(uploadFileTypeMock)));
        }
    }

    @Test
    void Should_CreateOrUpdate_When_ProjectDomainMirrorIsPresent() {

        // given
        UUID projectId = new UUID(0, 0);
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(projectId);

        ProjectDomainMirror projectDomainMirrorMock = mock(ProjectDomainMirror.class);
        DomainMirror domainMirrorMock = mock(DomainMirror.class);

        when(repository.findByProjectId(eq(projectId))).thenReturn(Optional.of(projectDomainMirrorMock));
        when(repository.save(projectDomainMirrorMock)).thenReturn(projectDomainMirrorMock);
        doNothing().when(regenerateDiagramsJobService).create(eq(projectMock));

        // when
        ProjectDomainMirror result = projectDomainMirrorService.createOrUpdate(projectMock, domainMirrorMock);

        // then
        assertThat(result).isEqualTo(projectDomainMirrorMock);
        verify(repository, times(1)).findByProjectId(eq(projectId));
        verify(regenerateDiagramsJobService, times(1)).create(eq(projectMock));
        verify(repository, times(1)).save(projectDomainMirrorMock);
    }

    @Test
    void Should_CreateOrUpdate_When_ProjectDomainMirrorIsNotPresent() {

        // given
        UUID projectId = new UUID(0, 0);
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(projectId);

        ProjectDomainMirror projectDomainMirrorMock = mock(ProjectDomainMirror.class);
        DomainMirror domainMirrorMock = mock(DomainMirror.class);

        when(repository.findByProjectId(eq(projectId))).thenReturn(Optional.empty());
        when(repository.save(any(ProjectDomainMirror.class))).thenReturn(projectDomainMirrorMock);

        // when
        ProjectDomainMirror result = projectDomainMirrorService.createOrUpdate(projectMock, domainMirrorMock);

        // then
        assertThat(result).isEqualTo(projectDomainMirrorMock);
        verify(repository, times(1)).findByProjectId(eq(projectId));
        verify(repository, times(1)).save(projectDomainMirrorArgumentCaptor.capture());
        assertThat(projectDomainMirrorArgumentCaptor.getValue().getDomainMirror()).isEqualTo(domainMirrorMock);
    }

    @Test
    void Should_Delete_When_ProjectDomainMirrorIsPresent() {

        // given
        UUID projectId = new UUID(0, 0);
        ProjectDomainMirror projectDomainMirrorMock = mock(ProjectDomainMirror.class);

        when(repository.findByProjectId(eq(projectId))).thenReturn(Optional.of(projectDomainMirrorMock));
        doNothing().when(repository).delete(projectDomainMirrorMock);

        // when
        projectDomainMirrorService.delete(projectId);

        // then
        verify(repository, times(1)).findByProjectId(eq(projectId));
        verify(repository, times(1)).delete(eq(projectDomainMirrorMock));
    }
}