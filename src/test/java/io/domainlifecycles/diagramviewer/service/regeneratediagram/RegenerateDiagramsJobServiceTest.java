package io.domainlifecycles.diagramviewer.service.regeneratediagram;

import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.RegenerateDiagramsJobRepository;
import io.domainlifecycles.diagramviewer.service.RegenerateDiagramsJobServiceImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegenerateDiagramsJobServiceTest {

    @Captor
    ArgumentCaptor<RegenerateDiagramsJob> regenerateDiagramsJobCaptor;

    @Mock
    RegenerateDiagramsJobRepository repository;

    @InjectMocks
    RegenerateDiagramsJobServiceImpl service;

    @Test
    void Should_GetAllJobs() {

        // given
        when(repository.findAll()).thenReturn(
            Arrays.asList(mock(RegenerateDiagramsJob.class), mock(RegenerateDiagramsJob.class)));

        // when
        List<RegenerateDiagramsJob> result = service.getAll();

        // then
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void Should_CreateJobsForAllDiagrams_When_ProjectHasDiagrams() {

        // given
        Project project = mock(Project.class);
        Diagram firstDiagramMock = mock(Diagram.class);
        when(project.getDiagrams()).thenReturn(Set.of(firstDiagramMock));

        when(repository.save(any())).thenReturn(mock(RegenerateDiagramsJob.class));

        // when
        service.create(project);

        // then
        verify(repository, times(1)).save(regenerateDiagramsJobCaptor.capture());
        assertThat(regenerateDiagramsJobCaptor.getValue().getDiagram()).isEqualTo(firstDiagramMock);
    }

    @Test
    void Should_DeleteJob() {

        // given
        RegenerateDiagramsJob regenerateDiagramsJobMock = mock(RegenerateDiagramsJob.class);
        doNothing().when(repository).delete(eq(regenerateDiagramsJobMock));

        // when
        service.delete(regenerateDiagramsJobMock);

        // then
        verify(repository, times(1)).delete(eq(regenerateDiagramsJobMock));
    }

    @Test
    void Should_DeleteAllJobsForDiagram() {

        // given
        RegenerateDiagramsJob firstRegenerateDiagramsJobMock = mock(RegenerateDiagramsJob.class);
        RegenerateDiagramsJob secondRegenerateDiagramsJobMock = mock(RegenerateDiagramsJob.class);

        UUID diagramId = new UUID(0, 0);
        Diagram diagramMock = mock(Diagram.class);
        when(diagramMock.getId()).thenReturn(diagramId);

        when(repository.findByDiagramId(eq(diagramId))).thenReturn(List.of(firstRegenerateDiagramsJobMock, secondRegenerateDiagramsJobMock));

        // when
        service.delete(diagramMock);

        // then
        verify(repository, times(1)).delete(eq(firstRegenerateDiagramsJobMock));
        verify(repository, times(1)).delete(eq(secondRegenerateDiagramsJobMock));
    }
}