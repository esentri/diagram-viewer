package io.domainlifecycles.diagramviewer.service.diagramdirectory;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramDirectoryRepository;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryServiceImpl;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
public class DiagramDirectoryServiceTest {

    @Mock
    DiagramDirectoryRepository diagramDirectoryRepository;

    @Mock
    DiagramService diagramService;

    @InjectMocks
    DiagramDirectoryServiceImpl service;

    @Test
    public void Should_GetDiagramDirectoryByName_When_DiagramDirectoryWithNameExists() {

        // given
        String name = "testDirectory";
        DiagramDirectory diagramDirectory = DiagramDirectory.builder().name(name).build();

        // prepare mocks
        when(diagramDirectoryRepository.findByName(eq(name))).thenReturn(Optional.of(diagramDirectory));

        // when
        DiagramDirectory diagramDirectoryResult = service.getByName(name);

        // then
        assertThat(diagramDirectoryResult).isNotNull();
        assertThat(diagramDirectoryResult).isEqualTo(diagramDirectory);
    }

    @Test
    public void Should_ThrowDiagramViewerException_When_DiagramDirectoryWithNameDoesNotExist() {

        // given
        String name = "testDirectory";

        // prepare mocks
        when(diagramDirectoryRepository.findByName(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> service.getByName(name)).isInstanceOf(DiagramViewerException.class).hasMessage("No Diagram Directory found with name '" + name + "'.");
    }

    @Test
    public void Should_CreateDiagramDirectory_When_DiagramDirectoryExists() {

        // given
        String name = "testDirectory";

        // prepare mocks
        Diagram diagramMock1 = mock(Diagram.class);
        Diagram diagramMock2 = mock(Diagram.class);
        Set<Diagram> diagramMocks = Set.of(diagramMock1, diagramMock2);

        Project projectMock = mock(Project.class);
        doNothing().when(projectMock).addDiagramDirectory(any(DiagramDirectory.class));

        // when
        service.create(name, projectMock, diagramMocks);

        // then
        verify(diagramDirectoryRepository, times(1)).save(any(DiagramDirectory.class));
        verify(projectMock, times(1)).addDiagramDirectory(any(DiagramDirectory.class));

        verify(diagramMock1, times(1)).setDiagramDirectory(any(DiagramDirectory.class));
        verify(diagramService, times(1)).update(eq(diagramMock1));

        verify(diagramMock2, times(1)).setDiagramDirectory(any(DiagramDirectory.class));
        verify(diagramService, times(1)).update(eq(diagramMock2));
    }

    @Test
    public void Should_AddDiagramToDiagramDirectory_When_DiagramDirectoryExists() {

        // prepare mocks
        Diagram diagramMock = mock(Diagram.class);

        DiagramDirectory diagramDirectoryMock = mock(DiagramDirectory.class);
        doNothing().when(diagramDirectoryMock).addDiagram(eq(diagramMock));

        // when
        service.add(diagramDirectoryMock, diagramMock);

        // then
        verify(diagramDirectoryMock, times(1)).addDiagram(eq(diagramMock));
        verify(diagramDirectoryRepository, times(1)).save(eq(diagramDirectoryMock));
        verify(diagramService, times(1)).update(eq(diagramMock));
    }

    @Test
    public void Should_UpdateDiagramDirectory_When_DiagramDirectoryExists() {

        // given
        String name = "newTestDirectoryName";

        // prepare mocks
        DiagramDirectory diagramDirectoryMock = mock(DiagramDirectory.class);
        doNothing().when(diagramDirectoryMock).setName(eq(name));

        // when
        service.update(diagramDirectoryMock, name);

        // then
        verify(diagramDirectoryMock, times(1)).setName(eq(name));
        verify(diagramDirectoryRepository, times(1)).save(eq(diagramDirectoryMock));
    }
}