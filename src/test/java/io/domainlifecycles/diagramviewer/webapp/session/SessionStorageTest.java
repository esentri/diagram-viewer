package io.domainlifecycles.diagramviewer.webapp.session;

import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.service.ProjectModelCache;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The session only holds UI state; the model data comes from the shared {@link ProjectModelCache}, whose loading
 * behaviour is covered by {@code ProjectModelCacheTest}.
 */
@ExtendWith(MockitoExtension.class)
class SessionStorageTest {

    @Mock
    ProjectDomainMirrorService projectDomainMirrorService;

    @Mock
    ProjectModelCache projectModelCache;

    SessionStorage sessionStorage;

    @BeforeEach
    void setUp() {
        sessionStorage = new SessionStorage(projectDomainMirrorService, projectModelCache);
    }

    @Test
    void Should_StoreFileUpload_And_PutItIntoTheSharedCache() {

        // given
        Project project = mock(Project.class);
        DomainMirror created = mock(DomainMirror.class);
        Path path = mock(Path.class);
        when(projectDomainMirrorService.createOrUpdate(project, Set.of("x"), path, UploadFileType.JSON)).thenReturn(created);

        // when
        sessionStorage.createOrUpdate(project, Set.of("x"), path, UploadFileType.JSON);

        // then
        verify(projectModelCache).putFromFileUpload(project, created);
    }

    @Test
    void Should_DeleteProjectModel_And_RemoveItFromTheSharedCache() {

        // given
        UUID projectId = UUID.randomUUID();

        // when
        sessionStorage.delete(projectId);

        // then
        verify(projectDomainMirrorService).delete(projectId);
        verify(projectModelCache).invalidate(projectId);
    }

    @Test
    void Should_KeepUiStatePerSession() {

        // when
        sessionStorage.setDomainTypeSettingOpen(DomainType.AGGREGATE_ROOT, true);
        sessionStorage.setFlowFilterOpen(true);

        // then
        assertThat(sessionStorage.isDomainTypeSettingOpen(DomainType.AGGREGATE_ROOT)).isTrue();
        assertThat(sessionStorage.isDomainTypeSettingOpen(DomainType.ENTITY)).isFalse();
        assertThat(sessionStorage.isFlowFilterOpen()).isTrue();
        assertThat(new SessionStorage(projectDomainMirrorService, projectModelCache).isFlowFilterOpen()).isFalse();
    }
}
