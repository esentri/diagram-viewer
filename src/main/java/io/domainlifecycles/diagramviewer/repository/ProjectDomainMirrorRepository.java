package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectDomainMirrorRepository extends CrudRepository<ProjectDomainMirror, UUID> {

    Optional<ProjectDomainMirror> findByProjectId(UUID projectId);

    @Query(
        value = """
                SELECT value AS mirror
                         FROM
                             project_domain_mirror,
                             jsonb_each(domain_mirror::jsonb -> 'allTypeMirrors') AS mirrors(key, value)
                         WHERE project_id = :id
                         AND value ->> '@class' = 'io.domainlifecycles.mirror.model.AggregateRootModel'
                """,
        nativeQuery = true
    )
    List<String> findProjectAggregateTypes(@Param("id") UUID projectId);

    @Query(
        value = """
                SELECT value AS mirror
                         FROM
                             project_domain_mirror,
                             jsonb_each(domain_mirror::jsonb -> 'allTypeMirrors') AS mirrors(key, value)
                         WHERE project_id = :id
                         AND value ->> '@class' <> 'io.domainlifecycles.mirror.model.EnumModel'
                         AND value ->> '@class' <> 'io.domainlifecycles.mirror.model.IdentityModel'
                """,
        nativeQuery = true
    )
    List<String> findProjectDomainTypesWithoutEnumsAndIds(@Param("id") UUID projectId);
}
