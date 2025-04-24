package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.Project;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project, UUID> {

    Optional<Project> findByName(String projectName);
}