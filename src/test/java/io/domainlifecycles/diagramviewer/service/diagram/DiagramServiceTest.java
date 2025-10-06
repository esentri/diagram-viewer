package io.domainlifecycles.diagramviewer.service.diagram;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagramServiceTest {

    @Mock
    SessionStorage sessionStorage;

    @Mock
    DiagramRepository repository;

    @Mock
    KrokiClient krokiClient;

    @InjectMocks
    DiagramServiceImpl diagramService;


    @Test
    public void Should_FindAllDiagramsWithProjectId() {

        // given
        UUID projectId = new UUID(0, 0);

        // prepare mocks
        Diagram diagramMock = mock(Diagram.class);
        when(repository.findByProjectId(eq(projectId))).thenReturn(Set.of(diagramMock));

        // when
        Set<Diagram> diagramsResult = diagramService.findAll(projectId);

        // then
        verify(repository, times(1)).findByProjectId(eq(projectId));
        assertThat(diagramsResult).containsExactly(diagramMock);
    }

    @Test
    public void Should_UpdateDiagram() {

        // prepare mocks
        Diagram diagramMock = mock(Diagram.class);
        when(repository.save(diagramMock)).thenReturn(diagramMock);

        // when
        Diagram updatedDiagramResult = diagramService.update(diagramMock);

        // then
        assertThat(updatedDiagramResult).isEqualTo(diagramMock);
        verify(repository, times(1)).save(diagramMock);
    }
}