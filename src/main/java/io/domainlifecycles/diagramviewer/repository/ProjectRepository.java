package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.Project;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

    Optional<Project> findByProjectNameClean(String projectNameClean);
    Optional<Project> findByAbsolutePathToTarget(String absolutePathToTarget);
}