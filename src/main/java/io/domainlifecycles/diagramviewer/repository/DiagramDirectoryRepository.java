package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagramDirectoryRepository extends CrudRepository<DiagramDirectory, UUID> {

    Optional<DiagramDirectory> findByName(String name);
}
