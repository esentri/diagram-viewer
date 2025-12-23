package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagramRepository extends CrudRepository<Diagram, UUID> {

    Optional<Diagram> findByName(String name);

    Set<Diagram> findByProjectId(UUID projectId);
}
