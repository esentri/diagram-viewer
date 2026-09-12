package io.domainlifecycles.diagramviewer.webapp.session;

import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.serialize.DomainCallsSerializer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionStorageTest {

    @Mock
    ProjectDomainMirrorService projectDomainMirrorService;

    @Mock
    ProjectRepository projectRepository;

    @Mock
    DomainCallsSerializer domainCallsSerializer;

    SessionStorage sessionStorage;

    @BeforeEach
    void setUp() {
        sessionStorage = new SessionStorage(projectDomainMirrorService, projectRepository, domainCallsSerializer);
    }

    @Test
    void Should_ReturnEmpty_When_NoDomainCallsWereUploaded() {

        // given
        UUID projectId = UUID.randomUUID();
        Instant latestChange = Instant.now();
        DomainMirror domainMirrorMock = mock(DomainMirror.class);
        ProjectDomainMirror projectDomainMirrorMock = ProjectDomainMirror.builder()
            .projectId(projectId)
            .domainMirror(domainMirrorMock)
            .domainCalls(null)
            .build();

        when(projectRepository.findLatestChange(projectId)).thenReturn(latestChange);
        when(projectDomainMirrorService.getByProjectId(projectId)).thenReturn(projectDomainMirrorMock);
        when(projectDomainMirrorService.getAllAggregateRootMirrors(projectId)).thenReturn(List.of());
        when(projectDomainMirrorService.getAllDomainTypeMirrorsWithoutEnumsAndIds(projectId)).thenReturn(List.of());

        // when
        Optional<DomainCalls> result = sessionStorage.getDomainCalls(projectId);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(domainCallsSerializer);
    }

    @Test
    void Should_ReturnDeserializedDomainCallsAndCacheIt_When_DomainCallsWereUploaded() {

        // given
        UUID projectId = UUID.randomUUID();
        Instant latestChange = Instant.now();
        DomainMirror domainMirrorMock = mock(DomainMirror.class);
        DomainCalls domainCallsMock = mock(DomainCalls.class);
        String domainCallsJson = "{\"callsByCaller\":[],\"diagnostics\":[]}";
        ProjectDomainMirror projectDomainMirrorMock = ProjectDomainMirror.builder()
            .projectId(projectId)
            .domainMirror(domainMirrorMock)
            .domainCalls(domainCallsJson)
            .build();

        when(projectRepository.findLatestChange(projectId)).thenReturn(latestChange);
        when(projectDomainMirrorService.getByProjectId(projectId)).thenReturn(projectDomainMirrorMock);
        when(projectDomainMirrorService.getAllAggregateRootMirrors(projectId)).thenReturn(List.of());
        when(projectDomainMirrorService.getAllDomainTypeMirrorsWithoutEnumsAndIds(projectId)).thenReturn(List.of());
        when(domainCallsSerializer.deserialize(domainCallsJson, domainMirrorMock)).thenReturn(domainCallsMock);

        // when
        Optional<DomainCalls> firstResult = sessionStorage.getDomainCalls(projectId);
        Optional<DomainCalls> secondResult = sessionStorage.getDomainCalls(projectId);

        // then
        assertThat(firstResult).contains(domainCallsMock);
        assertThat(secondResult).contains(domainCallsMock);
        // deserialized once when the container is built, then served from the session on the second call
        verify(domainCallsSerializer, times(1)).deserialize(domainCallsJson, domainMirrorMock);
        verify(projectDomainMirrorService, times(1)).getByProjectId(projectId);
    }

    @Test
    void Should_CacheDomainCallsFromUpload_When_CreateOrUpdateIsCalled() {

        // given
        UUID projectId = UUID.randomUUID();
        Instant latestChange = Instant.now();
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(projectId);
        when(projectMock.getLatestChangeInstant()).thenReturn(latestChange);

        DomainMirror domainMirrorMock = mock(DomainMirror.class);
        when(domainMirrorMock.getAllAggregateRootMirrors()).thenReturn(List.of());
        when(domainMirrorMock.getAllDomainTypeMirrors()).thenReturn(List.of());

        DomainCalls domainCallsMock = mock(DomainCalls.class);
        String domainCallsJson = "{\"callsByCaller\":[],\"diagnostics\":[]}";
        ProjectDomainMirror projectDomainMirrorMock = ProjectDomainMirror.builder()
            .projectId(projectId)
            .domainMirror(domainMirrorMock)
            .domainCalls(domainCallsJson)
            .build();

        when(projectDomainMirrorService.createOrUpdate(projectMock, domainMirrorMock, domainCallsJson))
            .thenReturn(projectDomainMirrorMock);
        when(domainCallsSerializer.deserialize(domainCallsJson, domainMirrorMock)).thenReturn(domainCallsMock);
        when(projectRepository.findLatestChange(projectId)).thenReturn(latestChange);

        // when
        sessionStorage.createOrUpdate(projectMock, domainMirrorMock, domainCallsJson);
        Optional<DomainCalls> result = sessionStorage.getDomainCalls(projectId);

        // then
        assertThat(result).contains(domainCallsMock);
        // the container built during createOrUpdate is reused, no additional lookup against the persisted project
        verify(projectDomainMirrorService, never()).getByProjectId(projectId);
    }
}
