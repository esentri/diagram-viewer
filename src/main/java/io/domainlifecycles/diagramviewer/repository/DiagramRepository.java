package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.Diagram;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagramRepository extends CrudRepository<Diagram, Long> {
}
