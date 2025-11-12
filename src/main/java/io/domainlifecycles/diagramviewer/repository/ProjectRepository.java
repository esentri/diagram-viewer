package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.viewer.Project;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project, UUID> {

    Optional<Project> findByName(String projectName);

    @Query("""
    SELECT 
        CASE 
            WHEN p.changedAt IS NOT NULL THEN p.changedAt 
            ELSE p.createdAt 
        END
    FROM Project p
    WHERE p.id = :projectId
    """)
    Instant findLatestChange(@Param("projectId") UUID projectId);
}