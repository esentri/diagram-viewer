package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegenerateDomainMirrorJobRepository extends CrudRepository<RegenerateDiagramsJob, UUID> {

    List<RegenerateDiagramsJob> findByDiagramId(UUID diagramId);
}
