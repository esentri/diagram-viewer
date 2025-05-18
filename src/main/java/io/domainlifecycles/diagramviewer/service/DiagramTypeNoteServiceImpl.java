package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.Optional;
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
            diagramTypeNoteForPersist.setNotes(notes);
        }
        else {
            diagramTypeNoteForPersist = DiagramTypeNote.builder()
                .notes(notes)
                .domainTypeMirrorName(domainTypeMirror.getTypeName())
                .diagram(diagram)
                .build();
        }

        repository.save(diagramTypeNoteForPersist);
    }

    @Override
    public String getNotes(Diagram diagram, DomainTypeMirror domainTypeMirror) {
        if(domainTypeMirror == null) return "";

        Optional<DiagramTypeNote> foundNotes = repository.findByDiagramIdAndDomainTypeMirrorName(
            diagram.getId(), domainTypeMirror.getTypeName());
        return foundNotes.isEmpty() ? "" : foundNotes.get().getNotes();
    }
}
