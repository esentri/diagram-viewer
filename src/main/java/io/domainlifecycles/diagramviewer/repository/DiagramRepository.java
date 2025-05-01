package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.Diagram;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagramRepository extends CrudRepository<Diagram, UUID> {

    Optional<Diagram> findByFileName(String fileName);
}
