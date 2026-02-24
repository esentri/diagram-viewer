/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
