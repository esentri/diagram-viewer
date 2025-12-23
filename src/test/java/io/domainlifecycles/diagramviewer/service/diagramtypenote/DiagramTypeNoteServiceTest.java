package io.domainlifecycles.diagramviewer.service.diagramtypenote;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteServiceImpl;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;
import java.util.Map;
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

    @Captor
    ArgumentCaptor<List<DiagramTypeNote>> diagramTypeNoteListCaptor;

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

    @Test
    void Should_DeleteNoteForTypeMirror_When_NotePresent() {

        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DomainTypeMirror domainTypeMirrorMock = mock(DomainTypeMirror.class);
        String typeName = "testTypeName";
        when(domainTypeMirrorMock.getTypeName()).thenReturn(typeName);

        DiagramTypeNote diagramTypeNoteMock = mock(DiagramTypeNote.class);
        when(diagramTypeNoteRepository.findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(typeName))).thenReturn(Optional.of(
            diagramTypeNoteMock));

        // when
        diagramTypeNoteService.delete(domainTypeMirrorMock, diagramMock);

        // then
        verify(diagramTypeNoteRepository, times(1)).findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(typeName));
        verify(diagramTypeNoteRepository, times(1)).delete(diagramTypeNoteMock);
    }

    @Test
    void Should_NotDeleteNoteForTypeMirror_When_NoNotesPresent() {

        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DomainTypeMirror domainTypeMirrorMock = mock(DomainTypeMirror.class);
        String typeName = "testTypeName";
        when(domainTypeMirrorMock.getTypeName()).thenReturn(typeName);

        when(diagramTypeNoteRepository.findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(typeName))).thenReturn(Optional.empty());

        // when
        diagramTypeNoteService.delete(domainTypeMirrorMock, diagramMock);

        // then
        verify(diagramTypeNoteRepository, times(1)).findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(typeName));
        verify(diagramTypeNoteRepository, never()).delete(any());
    }

    @Test
    void Should_DeleteAllTypeNotesForDiagram_When_NotesPresent() {
        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DiagramTypeNote firstDiagramTypeNoteMock = mock(DiagramTypeNote.class);
        DiagramTypeNote secondDiagramTypeNoteMock = mock(DiagramTypeNote.class);

        when(diagramTypeNoteRepository.findByDiagramId(eq(diagramId))).thenReturn(List.of(firstDiagramTypeNoteMock,
            secondDiagramTypeNoteMock));

        // when
        diagramTypeNoteService.delete(diagramMock);

        // then
        verify(diagramTypeNoteRepository, times(1)).findByDiagramId(eq(diagramId));
        verify(diagramTypeNoteRepository, times(1)).deleteAll(any());
    }

    @Test
    void Should_GetEmptyNotes_When_DomainTypeMirrorIsNull() {

        // when
        String result =  diagramTypeNoteService.getNotes(mock(Diagram.class), null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void Should_GetEmptyNotes_When_NoDiagramTypeNotesFound() {

        // given
        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DomainTypeMirror domainTypeMirrorMock = mock(DomainTypeMirror.class);
        String typeName = "testTypeName";
        when(domainTypeMirrorMock.getTypeName()).thenReturn(typeName);

        when(diagramTypeNoteRepository.findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(typeName)))
            .thenReturn(Optional.empty());

        // when
        String result =  diagramTypeNoteService.getNotes(diagramMock, domainTypeMirrorMock);

        // then
        assertThat(result).isEmpty();
        verify(diagramTypeNoteRepository, times(1)).findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(typeName));
    }

    @Test
    void Should_GetNotes_When_DiagramTypeNotesFound() {

        // given
        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DomainTypeMirror domainTypeMirrorMock = mock(DomainTypeMirror.class);
        String typeName = "testTypeName";
        when(domainTypeMirrorMock.getTypeName()).thenReturn(typeName);

        DiagramTypeNote diagramTypeNoteMock = mock(DiagramTypeNote.class);
        String testNotes = "testNotes";
        when(diagramTypeNoteMock.getNotes()).thenReturn(testNotes);

        when(diagramTypeNoteRepository.findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(typeName)))
            .thenReturn(Optional.of(diagramTypeNoteMock));

        // when
        String result =  diagramTypeNoteService.getNotes(diagramMock, domainTypeMirrorMock);

        // then
        assertThat(result).isEqualTo(testNotes);
        verify(diagramTypeNoteRepository, times(1)).findByDiagramIdAndDomainTypeMirrorName(eq(diagramId), eq(typeName));
    }

    @Test
    void Should_GetAllNotesForDiagram_When_NotesPresent() {

        // given
        Diagram diagramMock = mock(Diagram.class);
        UUID diagramId = new UUID(0, 0);
        when(diagramMock.getId()).thenReturn(diagramId);

        DiagramTypeNote firstDiagramTypeNoteMock = mock(DiagramTypeNote.class);
        String firstTypeName = "firstTypeName";
        when(firstDiagramTypeNoteMock.getDomainTypeMirrorName()).thenReturn(firstTypeName);
        String firstNotes = "firstNotes";
        when(firstDiagramTypeNoteMock.getNotes()).thenReturn(firstNotes);
        DiagramTypeNote secondDiagramTypeNoteMock = mock(DiagramTypeNote.class);
        String secondTypeName = "secondTypeName";
        when(secondDiagramTypeNoteMock.getDomainTypeMirrorName()).thenReturn(secondTypeName);
        String secondNotes = "secondNotes";
        when(secondDiagramTypeNoteMock.getNotes()).thenReturn(secondNotes);

        when(diagramTypeNoteRepository.findByDiagramId(eq(diagramId))).thenReturn(List.of(firstDiagramTypeNoteMock, secondDiagramTypeNoteMock));

        // when
        Map<String, String> result = diagramTypeNoteService.getNotes(diagramMock);

        assertThat(result.containsKey(firstTypeName)).isTrue();
        assertThat(result.containsKey(secondTypeName)).isTrue();
        assertThat(result.get(firstTypeName)).isEqualTo(firstNotes);
        assertThat(result.get(secondTypeName)).isEqualTo(secondNotes);
    }
}