package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagramTypeNoteRepository extends CrudRepository<DiagramTypeNote, UUID> {

    Optional<DiagramTypeNote> findByDiagramIdAndDomainTypeMirrorName(UUID diagramId, String domainTypeMirrorName);

    List<DiagramTypeNote> findByDiagramId(UUID diagramId);
}
