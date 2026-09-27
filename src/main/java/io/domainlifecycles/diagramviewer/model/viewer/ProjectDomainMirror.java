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

package io.domainlifecycles.diagramviewer.model.viewer;

import io.domainlifecycles.diagramviewer.model.converter.DomainModelConverter;
import io.domainlifecycles.mirror.api.DomainMirror;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "project_domain_mirror")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDomainMirror {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID projectId;

    /**
     * Legacy storage: the domain mirror as uncompressed JSON text. Only still set for projects last
     * uploaded before the compressed storage ({@link #domainMirrorGz}) was introduced; read as fallback.
     */
    @Column(columnDefinition = "TEXT")
    @Convert(converter = DomainModelConverter.class)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DomainMirror domainMirror;

    /**
     * The raw JSON representation of the static analysis result ({@code DomainCalls}) uploaded
     * alongside the domain mirror, or {@code null} if none was uploaded. Kept as raw JSON rather than
     * deserialized, since a {@code DomainCalls} is only meaningful resolved against the exact
     * {@link DomainMirror} instance it was analyzed against.
     */
    @Column(columnDefinition = "TEXT")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String domainCalls;

    /**
     * The domain mirror as gzip-compressed JSON. PostgreSQL limits a single field value to 1 GB, which
     * the uncompressed JSON of a large domain model exceeds; compressed, it is a fraction of that.
     * Excluded from {@code toString}/{@code equals}, as it can be hundreds of megabytes.
     */
    @Column(name = "domain_mirror_gz")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] domainMirrorGz;

    /**
     * The static analysis result ({@code DomainCalls}) as gzip-compressed JSON, or {@code null} if none
     * was uploaded. See {@link #domainMirrorGz}.
     */
    @Column(name = "domain_calls_gz")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] domainCallsGz;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;

}
