package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DiagramTypeNoteServiceImpl implements DiagramTypeNoteService {

    private final DiagramTypeNoteRepository repository;

    public DiagramTypeNoteServiceImpl(DiagramTypeNoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(String notes, DomainTypeMirror domainTypeMirror, Diagram diagram) {
        if(notes.length() > DiagramTypeNote.NOTES_MAX_LENGTH)
            throw DiagramViewerException.fail("Diagram type notes may not be longer than 4000 characters.");

        Optional<DiagramTypeNote> foundNotes = repository.findByDiagramIdAndDomainTypeMirrorName(
            diagram.getId(), domainTypeMirror.getTypeName());

        DiagramTypeNote diagramTypeNoteForPersist;

        if(foundNotes.isPresent()) {
            diagramTypeNoteForPersist = foundNotes.get();

            if(notes.isBlank()) {
                repository.delete(diagramTypeNoteForPersist);
                return;
            }

            diagramTypeNoteForPersist.setNotes(notes);
        }
        else {
            if(notes.isBlank()) {
                return;
            }

            diagramTypeNoteForPersist = DiagramTypeNote.builder()
                .notes(notes)
                .domainTypeMirrorName(domainTypeMirror.getTypeName())
                .diagram(diagram)
                .build();
        }

        repository.save(diagramTypeNoteForPersist);
    }

    @Override
    public void delete(DomainTypeMirror typeMirror, Diagram diagram) {
        Optional<DiagramTypeNote> foundDiagramTypeNote = repository.findByDiagramIdAndDomainTypeMirrorName(
            diagram.getId(), typeMirror.getTypeName());
        foundDiagramTypeNote.ifPresent(repository::delete);
    }

    @Override
    public void delete(Diagram diagram) {
        List<DiagramTypeNote> allTypeNotesForDiagram = repository.findByDiagramId(diagram.getId());
        repository.deleteAll(allTypeNotesForDiagram);
    }

    @Override
    public String getNotes(Diagram diagram, DomainTypeMirror domainTypeMirror) {
        if(domainTypeMirror == null) return "";

        Optional<DiagramTypeNote> foundNotes = repository.findByDiagramIdAndDomainTypeMirrorName(
            diagram.getId(), domainTypeMirror.getTypeName());
        return foundNotes.isEmpty() ? "" : foundNotes.get().getNotes();
    }

    @Override
    public Map<String, String> getNotes(Diagram diagram) {
        List<DiagramTypeNote> allDiagramTypeNotes = repository.findByDiagramId(diagram.getId());
        return allDiagramTypeNotes.stream().collect(Collectors.toMap(DiagramTypeNote::getDomainTypeMirrorName,
            DiagramTypeNote::getNotes));
    }
}
