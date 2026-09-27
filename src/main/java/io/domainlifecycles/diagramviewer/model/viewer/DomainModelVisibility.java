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
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> explicitlyIncludedPackagesNames = new HashSet<>();

    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> includeConnectedToClassNames = new HashSet<>();

    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_incl_ing", // short, unique table name
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "class_name")
    private Set<String> includeConnectedToIngoingClassNames = new HashSet<>();

    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_incl_out",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "class_name")
    private Set<String> includeConnectedToOutgoingClassNames = new HashSet<>();

    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_excl_ing",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "class_name")
    private Set<String> excludeConnectedToIngoingClassNames = new HashSet<>();

    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_excl_out",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "class_name")
    private Set<String> excludeConnectedToOutgoingClassNames = new HashSet<>();

    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> blacklistedClassNames = new HashSet<>();

    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> inlinedValueObjects = new HashSet<>();

    /**
     * The flow starting points the diagram is restricted to, a full qualified type name each,
     * optionally followed by {@code #} and a method name. Empty means the diagram is not restricted
     * to any flow.
     */
    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_flows",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "flow_starting_point")
    private Set<String> includeFlowsFrom = new HashSet<>();

    /**
     * The flow target points the diagram is restricted to - the backward counterpart of
     * {@link #includeFlowsFrom}: instead of "what does this lead to", the diagram shows "what leads
     * into this". Same syntax (a full qualified type name each, optionally followed by {@code #} and
     * a method name), except that a domain command can never be a target. Empty means the diagram is
     * not restricted to any backward flow.
     */
    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_flows_to",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "flow_target_point")
    private Set<String> includeFlowsTo = new HashSet<>();

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
            Set<String> inlinedValueObjects,
            Set<String> includeFlowsFrom,
            Set<String> includeFlowsTo
    ) {
        this.explicitlyIncludedPackagesNames = explicitlyIncludedPackagesNames == null ? new HashSet<>() : explicitlyIncludedPackagesNames;
        this.includeConnectedToIngoingClassNames = includeConnectedToIngoingClassNames == null ? new HashSet<>() : includeConnectedToIngoingClassNames;
        this.includeConnectedToOutgoingClassNames = includeConnectedToOutgoingClassNames == null ? new HashSet<>() : includeConnectedToOutgoingClassNames;
        this.excludeConnectedToIngoingClassNames = excludeConnectedToIngoingClassNames == null ? new HashSet<>() : excludeConnectedToIngoingClassNames;
        this.excludeConnectedToOutgoingClassNames = excludeConnectedToOutgoingClassNames == null ? new HashSet<>() : excludeConnectedToOutgoingClassNames;
        this.includeConnectedToClassNames = includeConnectedToClassNames == null ? new HashSet<>() : includeConnectedToClassNames;
        this.blacklistedClassNames = blacklistedClassNames == null ? new HashSet<>() : blacklistedClassNames;
        this.inlinedValueObjects = inlinedValueObjects == null ? new HashSet<>() : inlinedValueObjects;
        this.includeFlowsFrom = includeFlowsFrom == null ? new HashSet<>() : includeFlowsFrom;
        this.includeFlowsTo = includeFlowsTo == null ? new HashSet<>() : includeFlowsTo;
    }

    /**
     * @return {@code true} if the diagram is restricted to at least one flow, forward or backward - only
     * then does rendering it need the project's static analysis result
     */
    public boolean hasFlowSettings() {
        return (includeFlowsFrom != null && !includeFlowsFrom.isEmpty())
            || (includeFlowsTo != null && !includeFlowsTo.isEmpty());
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
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
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
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
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
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
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
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
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
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
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
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
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
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
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
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
        );
    }

    public DomainModelVisibility replaceIncludeFlowsFrom(Set<String> includeFlowsFrom) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
        );
    }

    public DomainModelVisibility replaceIncludeFlowsTo(Set<String> includeFlowsTo) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames,
                inlinedValueObjects,
                includeFlowsFrom,
                includeFlowsTo
        );
    }
}
