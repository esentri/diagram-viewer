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
import io.domainlifecycles.mirror.api.DomainMirror;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectDomainMirrorRepository extends CrudRepository<ProjectDomainMirror, UUID> {

    Optional<ProjectDomainMirror> findByProjectId(UUID projectId);

    boolean existsByProjectId(UUID projectId);

    /**
     * Loads only the gzip-compressed domain mirror of a project, without its static analysis result.
     * Empty for projects last uploaded before the compressed storage was introduced.
     */
    @Query(value = "SELECT domain_mirror_gz FROM project_domain_mirror WHERE project_id = :id AND domain_mirror_gz IS NOT NULL",
        nativeQuery = true)
    Optional<byte[]> findDomainMirrorGzByProjectId(@Param("id") UUID projectId);

    /**
     * Loads only the gzip-compressed static analysis result ({@code DomainCalls}) of a project. Empty if
     * none was uploaded, or for projects last uploaded before the compressed storage was introduced.
     */
    @Query(value = "SELECT domain_calls_gz FROM project_domain_mirror WHERE project_id = :id AND domain_calls_gz IS NOT NULL",
        nativeQuery = true)
    Optional<byte[]> findDomainCallsGzByProjectId(@Param("id") UUID projectId);

    /**
     * Legacy storage: loads only the uncompressed domain mirror of a project last uploaded before the
     * compressed storage was introduced.
     */
    @Query("SELECT p.domainMirror FROM ProjectDomainMirror p WHERE p.projectId = :id")
    Optional<DomainMirror> findLegacyDomainMirrorByProjectId(@Param("id") UUID projectId);

    /**
     * Legacy storage: loads only the uncompressed static analysis result of a project last uploaded
     * before the compressed storage was introduced.
     */
    @Query("SELECT p.domainCalls FROM ProjectDomainMirror p WHERE p.projectId = :id")
    Optional<String> findLegacyDomainCallsJsonByProjectId(@Param("id") UUID projectId);

    /**
     * Checks whether a static analysis result was uploaded for a project, in either storage, without loading it.
     */
    @Query(value = """
                SELECT EXISTS (SELECT 1 FROM project_domain_mirror
                                WHERE project_id = :id AND (domain_calls_gz IS NOT NULL OR domain_calls IS NOT NULL))
                """, nativeQuery = true)
    boolean existsDomainCallsByProjectId(@Param("id") UUID projectId);

    /**
     * Replaces the domain mirror and static analysis result of a project with the given gzip-compressed
     * JSON - without loading the previous values - and clears the legacy uncompressed columns. The
     * {@code CAST}s keep a {@code null} static analysis result from being bound with the wrong type.
     */
    @Modifying
    @Query(value = """
                UPDATE project_domain_mirror
                   SET domain_mirror_gz = CAST(:domainMirrorGz AS bytea),
                       domain_calls_gz = CAST(:domainCallsGz AS bytea),
                       domain_mirror = NULL,
                       domain_calls = NULL,
                       changed_at = now()
                 WHERE project_id = :projectId
                """, nativeQuery = true)
    int updateCompressed(@Param("projectId") UUID projectId,
                         @Param("domainMirrorGz") byte[] domainMirrorGz,
                         @Param("domainCallsGz") byte[] domainCallsGz);

    /**
     * Stores the domain mirror and static analysis result of a project as gzip-compressed JSON.
     */
    @Modifying
    @Query(value = """
                INSERT INTO project_domain_mirror (id, project_id, domain_mirror_gz, domain_calls_gz, created_at, changed_at)
                VALUES (:id, :projectId, CAST(:domainMirrorGz AS bytea), CAST(:domainCallsGz AS bytea), now(), now())
                """, nativeQuery = true)
    void insertCompressed(@Param("id") UUID id,
                          @Param("projectId") UUID projectId,
                          @Param("domainMirrorGz") byte[] domainMirrorGz,
                          @Param("domainCallsGz") byte[] domainCallsGz);

    /**
     * Deletes the domain model of a project without loading it first.
     */
    @Modifying
    @Query("DELETE FROM ProjectDomainMirror p WHERE p.projectId = :id")
    void deleteByProjectIdWithoutLoading(@Param("id") UUID projectId);
}
