package io.domainlifecycles.diagramviewer.service.diagramtypenote;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteServiceImpl;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagramTypeNoteServiceTest {

    @Captor
    ArgumentCaptor<DiagramTypeNote> diagramTypeNoteCaptor;

    @Mock
    private DiagramTypeNoteRepository diagramTypeNoteRepository;

    private DiagramTypeNoteService diagramTypeNoteService;

    @BeforeEach
    void setUp() {
        diagramTypeNoteService = new DiagramTypeNoteServiceImpl(diagramTypeNoteRepository);
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnSave_When_NotesAreTooLong() {

        // given
        String notes = "a".repeat(DiagramTypeNote.NOTES_MAX_LENGTH + 1);

        // when
        // then
        assertThatThrownBy(() -> diagramTypeNoteService.save(notes, mock(DomainTypeMirror.class), mock(Diagram.class)))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("Diagram type notes may not be longer than 4000 characters.");
    }

    @Test
    void Should_DeleteNote_When_NotesArePresentButBlank() {

        // given
        String notes = "";
        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DomainTypeMirror domainTypeMirrorMock = mock(DomainTypeMirror.class);
        String domainTypeMirrorName = "testTypeName";
        when(domainTypeMirrorMock.getTypeName()).thenReturn(domainTypeMirrorName);

        DiagramTypeNote diagramTypeNoteMock = mock(DiagramTypeNote.class);

        when(diagramTypeNoteRepository.findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(domainTypeMirrorName))).thenReturn(
            Optional.of(diagramTypeNoteMock));

        doNothing().when(diagramTypeNoteRepository).delete(diagramTypeNoteMock);

        // when
        diagramTypeNoteService.save(notes, domainTypeMirrorMock, diagramMock);

        // then
        verify(diagramTypeNoteRepository, times(1)).findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(domainTypeMirrorName));
        verify(diagramTypeNoteRepository, times(1)).delete(diagramTypeNoteMock);
    }

    @Test
    void Should_SaveNote_When_NotesArePresentAndNotBlank() {

        // given
        String notes = "some test notes";
        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DomainTypeMirror domainTypeMirrorMock = mock(DomainTypeMirror.class);
        String domainTypeMirrorName = "testTypeName";
        when(domainTypeMirrorMock.getTypeName()).thenReturn(domainTypeMirrorName);

        DiagramTypeNote diagramTypeNoteMock = mock(DiagramTypeNote.class);

        when(diagramTypeNoteRepository.findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(domainTypeMirrorName))).thenReturn(
            Optional.of(diagramTypeNoteMock));

        when(diagramTypeNoteRepository.save(diagramTypeNoteMock)).thenReturn(diagramTypeNoteMock);

        // when
        diagramTypeNoteService.save(notes, domainTypeMirrorMock, diagramMock);

        // then
        verify(diagramTypeNoteRepository, times(1)).findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(domainTypeMirrorName));
        verify(diagramTypeNoteMock, times(1)).setNotes(notes);
        verify(diagramTypeNoteRepository, times(1)).save(diagramTypeNoteMock);
    }

    @Test
    void Should_DoNothing_When_NotesAreNotPresentAndBlank() {

        // given
        String notes = "";
        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DomainTypeMirror domainTypeMirrorMock = mock(DomainTypeMirror.class);
        String domainTypeMirrorName = "testTypeName";
        when(domainTypeMirrorMock.getTypeName()).thenReturn(domainTypeMirrorName);

        when(diagramTypeNoteRepository.findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(domainTypeMirrorName))).thenReturn(
            Optional.empty());

        // when
        diagramTypeNoteService.save(notes, domainTypeMirrorMock, diagramMock);

        // then
        verify(diagramTypeNoteRepository, times(1)).findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(domainTypeMirrorName));
        verify(diagramTypeNoteRepository, never()).save(any());
    }

    @Test
    void Should_CreateNewNotes_When_NotesAreNotPresentAndNotBlank() {

        // given
        String notes = "some test notes";
        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DomainTypeMirror domainTypeMirrorMock = mock(DomainTypeMirror.class);
        String domainTypeMirrorName = "testTypeName";
        when(domainTypeMirrorMock.getTypeName()).thenReturn(domainTypeMirrorName);

        when(diagramTypeNoteRepository.findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(domainTypeMirrorName))).thenReturn(
            Optional.empty());

        DiagramTypeNote diagramTypeNoteMock = mock(DiagramTypeNote.class);
        when(diagramTypeNoteRepository.save(any())).thenReturn(diagramTypeNoteMock);

        // when
        diagramTypeNoteService.save(notes, domainTypeMirrorMock, diagramMock);

        // then
        verify(diagramTypeNoteRepository, times(1)).findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(domainTypeMirrorName));
        verify(diagramTypeNoteRepository, times(1)).save(diagramTypeNoteCaptor.capture());
        assertThat(diagramTypeNoteCaptor.getValue().getNotes()).isEqualTo(notes);
        assertThat(diagramTypeNoteCaptor.getValue().getDomainTypeMirrorName()).isEqualTo(domainTypeMirrorName);
        assertThat(diagramTypeNoteCaptor.getValue().getDiagram()).isEqualTo(diagramMock);
    }
}