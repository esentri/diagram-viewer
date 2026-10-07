package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.api.MethodMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.DomainMethod;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the project model cache shared by all sessions.
 */
@ExtendWith(MockitoExtension.class)
class ProjectModelCacheTest {

    @Mock
    ProjectDomainMirrorService projectDomainMirrorService;

    @Mock
    ProjectRepository projectRepository;

    ProjectModelCache cache;

    @BeforeEach
    void setUp() {
        cache = new ProjectModelCache(projectDomainMirrorService, projectRepository, 10, 60);
    }

    @Test
    void Should_LoadProjectOnlyOnce_And_ShareItBetweenSessions() {

        // given: two sessions of different users
        UUID projectId = UUID.randomUUID();
        DomainMirror mirror = givenProject(projectId, Instant.parse("2026-09-27T10:00:00Z"), false);
        SessionStorage firstSession = new SessionStorage(projectDomainMirrorService, cache);
        SessionStorage secondSession = new SessionStorage(projectDomainMirrorService, cache);

        // when: both open the same project
        DomainMirror seenByFirst = firstSession.getDomainMirror(projectId);
        DomainMirror seenBySecond = secondSession.getDomainMirror(projectId);

        // then: one shared instance, loaded once
        assertThat(seenByFirst).isSameAs(mirror).isSameAs(seenBySecond);
        verify(projectDomainMirrorService, times(1)).getDomainMirror(projectId);
    }

    @Test
    void Should_ReloadProject_When_ItChangedSinceItWasCached() {

        // given
        UUID projectId = UUID.randomUUID();
        DomainMirror oldMirror = mock(DomainMirror.class);
        DomainMirror newMirror = mock(DomainMirror.class);
        when(projectRepository.findLatestChange(projectId))
            .thenReturn(Instant.parse("2026-09-27T10:00:00Z"), Instant.parse("2026-09-27T11:00:00Z"));
        when(projectDomainMirrorService.getDomainMirror(projectId)).thenReturn(oldMirror, newMirror);

        // when
        DomainMirror first = cache.get(projectId).domainMirror();
        DomainMirror second = cache.get(projectId).domainMirror();

        // then
        assertThat(first).isSameAs(oldMirror);
        assertThat(second).isSameAs(newMirror);
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void Should_NotLoadDomainCalls_When_ProjectIsOpenedOrOnlyCheckedForThem() {

        // given
        UUID projectId = UUID.randomUUID();
        givenProject(projectId, Instant.now(), true);

        // when
        ProjectModel model = cache.get(projectId);

        // then
        assertThat(model.domainCallsAvailable()).isTrue();
        verify(projectDomainMirrorService, never()).loadDomainCalls(any(), any());
    }

    @Test
    void Should_LoadDomainCallsOnlyOnce_And_ShareThemBetweenSessions() {

        // given
        UUID projectId = UUID.randomUUID();
        DomainMirror mirror = givenProject(projectId, Instant.now(), true);
        DomainCalls domainCalls = mock(DomainCalls.class);
        when(projectDomainMirrorService.loadDomainCalls(projectId, mirror)).thenReturn(Optional.of(domainCalls));
        SessionStorage firstSession = new SessionStorage(projectDomainMirrorService, cache);
        SessionStorage secondSession = new SessionStorage(projectDomainMirrorService, cache);

        // when
        Optional<DomainCalls> seenByFirst = firstSession.getDomainCalls(projectId);
        Optional<DomainCalls> seenBySecond = secondSession.getDomainCalls(projectId);

        // then
        assertThat(seenByFirst).containsSame(domainCalls);
        assertThat(seenBySecond).containsSame(domainCalls);
        verify(projectDomainMirrorService, times(1)).loadDomainCalls(projectId, mirror);
    }

    @Test
    void Should_ReturnNoDomainCalls_When_NoneWereUploaded() {

        // given
        UUID projectId = UUID.randomUUID();
        givenProject(projectId, Instant.now(), false);

        // when / then
        assertThat(cache.get(projectId).domainCalls()).isEmpty();
        verify(projectDomainMirrorService, never()).loadDomainCalls(any(), any());
    }

    @Test
    void Should_DeriveTypeListsFromLoadedMirror_WithoutEnumsAndIdentities() {

        // given
        UUID projectId = UUID.randomUUID();
        DomainMirror mirror = givenProject(projectId, Instant.now(), false);
        DomainTypeMirror aggregateRoot = typeMirror("x.Order", DomainType.AGGREGATE_ROOT);
        DomainTypeMirror service = typeMirror("x.OrderService", DomainType.DOMAIN_SERVICE);
        DomainTypeMirror enumType = typeMirror("x.Status", DomainType.ENUM);
        DomainTypeMirror identity = typeMirror("x.OrderId", DomainType.IDENTITY);
        AggregateRootMirror aggregateRootMirror = mock(AggregateRootMirror.class);
        when(mirror.getAllDomainTypeMirrors()).thenReturn(List.of(aggregateRoot, service, enumType, identity));
        when(mirror.getAllAggregateRootMirrors()).thenReturn(List.of(aggregateRootMirror));

        // when
        ProjectModel model = cache.get(projectId);

        // then: taken from the mirror itself, not queried separately
        assertThat(model.domainTypeMirrors()).containsExactly(aggregateRoot, service);
        assertThat(model.aggregateRootMirrors()).containsExactly(aggregateRootMirror);
    }

    @Test
    void Should_EvictProjects_When_TheirEstimatedHeapExceedsTheBudget() {

        // given: a budget of 1 MB and two projects of about 700 KB each
        cache = new ProjectModelCache(projectDomainMirrorService, projectRepository, 1, 60);
        UUID firstProject = UUID.randomUUID();
        UUID secondProject = UUID.randomUUID();
        givenProjectOfElements(firstProject, 1000, false);
        givenProjectOfElements(secondProject, 1000, false);

        // when
        cache.get(firstProject);
        cache.get(secondProject);

        // then
        assertThat(cache.size()).isEqualTo(1);
        assertThat(cache.estimatedBytes()).isLessThanOrEqualTo(1024 * 1024);
    }

    @Test
    void Should_WeighProjectAgain_When_ItsDomainCallsWereLoaded() {

        // given: a budget of 1 MB, two small projects (70 and 140 KB), both fitting
        cache = new ProjectModelCache(projectDomainMirrorService, projectRepository, 1, 60);
        UUID flowProject = UUID.randomUUID();
        UUID otherProject = UUID.randomUUID();
        DomainMirror flowMirror = givenProjectOfElements(flowProject, 100, true);
        givenProjectOfElements(otherProject, 200, false);
        when(projectDomainMirrorService.loadDomainCalls(flowProject, flowMirror))
            .thenReturn(Optional.of(domainCallsWithCallSites(4000)));
        cache.get(flowProject);
        cache.get(otherProject);
        assertThat(cache.size()).isEqualTo(2);

        // when: a flow filter loads the static analysis result (about 880 KB)
        ProjectModel model = cache.get(flowProject);
        model.domainCalls();

        // then: the grown model counts against the budget, so one project had to go
        assertThat(model.estimatedBytes()).isEqualTo(100 * 700L + 4000 * 220L);
        assertThat(cache.size()).isEqualTo(1);
        assertThat(cache.estimatedBytes()).isLessThanOrEqualTo(1024 * 1024);
    }

    private DomainMirror givenProjectOfElements(UUID projectId, int elements, boolean domainCallsAvailable) {
        DomainMirror mirror = givenProject(projectId, Instant.now(), domainCallsAvailable);
        List<DomainTypeMirror> types = new ArrayList<>();
        for (int i = 0; i < elements; i++) {
            DomainTypeMirror type = mock(DomainTypeMirror.class);
            lenient().when(type.getTypeName()).thenReturn("x.Type" + i);
            lenient().when(type.getDomainType()).thenReturn(DomainType.AGGREGATE_ROOT);
            types.add(type);
        }
        lenient().when(mirror.getAllDomainTypeMirrors()).thenReturn(types);
        return mirror;
    }

    private static DomainCalls domainCallsWithCallSites(int callSites) {
        DomainMethod caller = new DomainMethod("x.Caller", mock(MethodMirror.class));
        DomainMethod called = new DomainMethod("x.Called", mock(MethodMirror.class));
        List<DomainCalls.CallSite> sites = new ArrayList<>();
        for (int line = 1; line <= callSites; line++) {
            sites.add(new DomainCalls.CallSite(called, "x.Caller", line));
        }
        return DomainCalls.builder().add(caller, sites).build();
    }

    @Test
    void Should_UseFileUploadWithoutLoading_And_ForgetDeletedProject() {

        // given
        UUID projectId = UUID.randomUUID();
        Instant changedAt = Instant.now();
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        when(project.getLatestChangeInstant()).thenReturn(changedAt);
        when(projectRepository.findLatestChange(projectId)).thenReturn(changedAt);
        DomainMirror uploadedMirror = mock(DomainMirror.class);

        // when: the mirror created from an uploaded file is cached ...
        cache.putFromFileUpload(project, uploadedMirror);

        // then: ... and served without loading it again, without a static analysis result
        assertThat(cache.get(projectId).domainMirror()).isSameAs(uploadedMirror);
        assertThat(cache.get(projectId).domainCallsAvailable()).isFalse();
        verify(projectDomainMirrorService, never()).getDomainMirror(any());

        // when
        cache.invalidate(projectId);

        // then
        assertThat(cache.size()).isZero();
    }

    @Test
    void Should_Fail_When_ProjectDoesNotExist() {

        // given
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findLatestChange(projectId)).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> cache.get(projectId))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("project not found");
    }

    private DomainMirror givenProject(UUID projectId, Instant changedAt, boolean hasDomainCalls) {
        DomainMirror mirror = mock(DomainMirror.class);
        lenient().when(projectRepository.findLatestChange(projectId)).thenReturn(changedAt);
        lenient().when(projectDomainMirrorService.getDomainMirror(projectId)).thenReturn(mirror);
        lenient().when(projectDomainMirrorService.hasDomainCalls(projectId)).thenReturn(hasDomainCalls);
        return mirror;
    }

    private static DomainTypeMirror typeMirror(String typeName, DomainType domainType) {
        DomainTypeMirror mirror = mock(DomainTypeMirror.class);
        lenient().when(mirror.getTypeName()).thenReturn(typeName);
        when(mirror.getDomainType()).thenReturn(domainType);
        return mirror;
    }
}
