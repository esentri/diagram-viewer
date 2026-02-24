/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
