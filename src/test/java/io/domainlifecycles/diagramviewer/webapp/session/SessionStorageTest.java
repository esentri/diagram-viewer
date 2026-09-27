package io.domainlifecycles.diagramviewer.webapp.session;

import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionStorageTest {

    @Mock
    ProjectDomainMirrorService projectDomainMirrorService;

    @Mock
    ProjectRepository projectRepository;

    SessionStorage sessionStorage;

    @BeforeEach
    void setUp() {
        sessionStorage = new SessionStorage(projectDomainMirrorService, projectRepository);
    }

    @Test
    void Should_ReturnEmpty_When_NoDomainCallsWereUploaded() {

        // given
        UUID projectId = UUID.randomUUID();
        givenOpenableProject(projectId, mock(DomainMirror.class), false);

        // when
        Optional<DomainCalls> result = sessionStorage.getDomainCalls(projectId);

        // then
        assertThat(result).isEmpty();
        assertThat(sessionStorage.hasDomainCalls(projectId)).isFalse();
        verify(projectDomainMirrorService, never()).loadDomainCalls(any(), any());
    }

    @Test
    void Should_NotLoadDomainCalls_When_ProjectIsOpenedOrOnlyCheckedForThem() {

        // given
        UUID projectId = UUID.randomUUID();
        givenOpenableProject(projectId, mock(DomainMirror.class), true);

        // when: the project is opened and only checked for a static analysis result
        sessionStorage.getDomainMirror(projectId);
        boolean available = sessionStorage.hasDomainCalls(projectId);

        // then: neither the full entity nor the static analysis result was loaded
        assertThat(available).isTrue();
        verify(projectDomainMirrorService, never()).getByProjectId(projectId);
        verify(projectDomainMirrorService, never()).loadDomainCalls(any(), any());
    }

    @Test
    void Should_LoadDomainCallsOnFirstAccessAndCacheThem_When_DomainCallsWereUploaded() {

        // given
        UUID projectId = UUID.randomUUID();
        DomainMirror domainMirrorMock = mock(DomainMirror.class);
        DomainCalls domainCallsMock = mock(DomainCalls.class);
        givenOpenableProject(projectId, domainMirrorMock, true);
        when(projectDomainMirrorService.loadDomainCalls(projectId, domainMirrorMock)).thenReturn(Optional.of(domainCallsMock));

        // when
        Optional<DomainCalls> firstResult = sessionStorage.getDomainCalls(projectId);
        Optional<DomainCalls> secondResult = sessionStorage.getDomainCalls(projectId);

        // then: loaded and deserialized once, on first access, then served from the session
        assertThat(firstResult).contains(domainCallsMock);
        assertThat(secondResult).contains(domainCallsMock);
        verify(projectDomainMirrorService, times(1)).loadDomainCalls(projectId, domainMirrorMock);
        verify(projectDomainMirrorService, times(1)).getDomainMirror(projectId);
    }

    @Test
    void Should_DeriveTypeListsFromLoadedMirror_WithoutEnumsAndIdentities_When_ProjectIsOpened() {

        // given
        UUID projectId = UUID.randomUUID();
        DomainMirror domainMirrorMock = mock(DomainMirror.class);
        DomainTypeMirror aggregateRoot = typeMirror("x.Order", DomainType.AGGREGATE_ROOT);
        DomainTypeMirror service = typeMirror("x.OrderService", DomainType.DOMAIN_SERVICE);
        DomainTypeMirror enumType = typeMirror("x.Status", DomainType.ENUM);
        DomainTypeMirror identity = typeMirror("x.OrderId", DomainType.IDENTITY);
        AggregateRootMirror aggregateRootMirror = mock(AggregateRootMirror.class);
        when(domainMirrorMock.getAllDomainTypeMirrors()).thenReturn(List.of(aggregateRoot, service, enumType, identity));
        when(domainMirrorMock.getAllAggregateRootMirrors()).thenReturn(List.of(aggregateRootMirror));
        givenOpenableProject(projectId, domainMirrorMock, false);

        // when
        List<DomainTypeMirror> types = sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(projectId);
        List<AggregateRootMirror> aggregates = sessionStorage.getAllAggregateRootMirrors(projectId);

        // then: taken from the mirror itself, not queried separately (performance plan items 1.3 / 2.4)
        assertThat(types).containsExactly(aggregateRoot, service);
        assertThat(aggregates).containsExactly(aggregateRootMirror);
    }

    private static DomainTypeMirror typeMirror(String typeName, DomainType domainType) {
        DomainTypeMirror mirror = mock(DomainTypeMirror.class);
        lenient().when(mirror.getTypeName()).thenReturn(typeName);
        when(mirror.getDomainType()).thenReturn(domainType);
        return mirror;
    }

    private void givenOpenableProject(UUID projectId, DomainMirror domainMirror, boolean hasDomainCalls) {
        when(projectRepository.findLatestChange(projectId)).thenReturn(Instant.now());
        when(projectDomainMirrorService.getDomainMirror(projectId)).thenReturn(domainMirror);
        when(projectDomainMirrorService.hasDomainCalls(projectId)).thenReturn(hasDomainCalls);
    }
}
