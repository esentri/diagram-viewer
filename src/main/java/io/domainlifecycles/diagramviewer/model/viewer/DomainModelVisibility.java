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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "domain_model_visibility")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DomainModelVisibility {

    @Id
    @GeneratedValue
    private UUID id;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> explicitlyIncludedPackagesNames;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> includeConnectedToClassNames;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_incl_ing", // short, unique table name
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "class_name")
    private Set<String> includeConnectedToIngoingClassNames;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_incl_out",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "class_name")
    private Set<String> includeConnectedToOutgoingClassNames;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_excl_ing",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "class_name")
    private Set<String> excludeConnectedToIngoingClassNames;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_excl_out",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "class_name")
    private Set<String> excludeConnectedToOutgoingClassNames;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> blacklistedClassNames;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> inlinedValueObjects;

    @Getter
    @CreationTimestamp
    private Instant createdAt;

    @Getter
    @UpdateTimestamp
    private Instant changedAt;

    public DomainModelVisibility(
            Set<String> explicitlyIncludedPackagesNames,
            Set<String> includeConnectedToClassNames,
            Set<String> includeConnectedToIngoingClassNames,
            Set<String> includeConnectedToOutgoingClassNames,
            Set<String> excludeConnectedToIngoingClassNames,
            Set<String> excludeConnectedToOutgoingClassNames,
            Set<String> blacklistedClassNames,
            Set<String> inlinedValueObjects
    ) {
        this.explicitlyIncludedPackagesNames = explicitlyIncludedPackagesNames == null ? new HashSet<>() : explicitlyIncludedPackagesNames;
        this.includeConnectedToIngoingClassNames = includeConnectedToIngoingClassNames == null ? new HashSet<>() : includeConnectedToIngoingClassNames;
        this.includeConnectedToOutgoingClassNames = includeConnectedToOutgoingClassNames == null ? new HashSet<>() : includeConnectedToOutgoingClassNames;
        this.excludeConnectedToIngoingClassNames = excludeConnectedToIngoingClassNames == null ? new HashSet<>() : excludeConnectedToIngoingClassNames;
        this.excludeConnectedToOutgoingClassNames = excludeConnectedToOutgoingClassNames == null ? new HashSet<>() : excludeConnectedToOutgoingClassNames;
        this.includeConnectedToClassNames = includeConnectedToClassNames == null ? new HashSet<>() : includeConnectedToClassNames;
        this.blacklistedClassNames = blacklistedClassNames == null ? new HashSet<>() : blacklistedClassNames;
        this.inlinedValueObjects = inlinedValueObjects == null ? new HashSet<>() : inlinedValueObjects;
    }

    public DomainModelVisibility replaceBlacklistedClassNames(Set<String> blacklistedClassNames) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects
        );
    }

    public DomainModelVisibility replaceIncludeConnectedToClassNames(Set<String> includeConnectedToClassNames) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects
        );
    }

    public DomainModelVisibility replaceIncludeConnectedToIngoingClassNames(Set<String> includeConnectedToIngoingClassNames) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects
        );
    }

    public DomainModelVisibility replaceIncludeConnectedToOutgoingClassNames(Set<String> includeConnectedToOutgoingClassNames) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects
        );
    }

    public DomainModelVisibility replaceExcludeConnectedToIngoingClassNames(Set<String> excludeConnectedToIngoingClassNames) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects
        );
    }

    public DomainModelVisibility replaceExcludeConnectedToOutgoingClassNames(Set<String> excludeConnectedToOutgoingClassNames) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects
        );
    }

    public DomainModelVisibility replaceExplicitlyIncludedPackagesNames(Set<String> explicitlyIncludedPackagesNames) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects
        );
    }

    public DomainModelVisibility replaceInlinedValueObjects(Set<String> inlinedValueObjects) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects
        );
    }
}
