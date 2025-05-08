package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.ProjectDomainMirror;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectDomainMirrorRepository extends CrudRepository<ProjectDomainMirror, UUID> {

    Optional<ProjectDomainMirror> findByProjectId(UUID projectId);
}
