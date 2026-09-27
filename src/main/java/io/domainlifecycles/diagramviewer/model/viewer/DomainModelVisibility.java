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

    /**
     * A package no type name starts with: the restriction to it leaves nothing - unlike an empty set of packages,
     * which restricts nothing.
     */
    public static final String NO_PACKAGE = "#none";

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

    /**
     * The root packages of the Bounded Contexts the diagram is restricted to. Empty means no restriction. Combined
     * with {@link #explicitlyIncludedPackagesNames}, see {@link #getEffectiveIncludedPackages()}.
     */
    @Getter
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dmv_bounded_contexts",
        joinColumns = @JoinColumn(name = "dmv_id")
    )
    @Column(name = "package_name")
    private Set<String> includedBoundedContextPackages = new HashSet<>();

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
        this(explicitlyIncludedPackagesNames, includeConnectedToClassNames, includeConnectedToIngoingClassNames,
            includeConnectedToOutgoingClassNames, excludeConnectedToIngoingClassNames, excludeConnectedToOutgoingClassNames,
            blacklistedClassNames, inlinedValueObjects, includeFlowsFrom, includeFlowsTo, null);
    }

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
            Set<String> includeFlowsTo,
            Set<String> includedBoundedContextPackages
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
        this.includedBoundedContextPackages = includedBoundedContextPackages == null ? new HashSet<>() : includedBoundedContextPackages;
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
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
                includeFlowsTo,
                includedBoundedContextPackages
        );
    }

    public DomainModelVisibility replaceIncludedBoundedContextPackages(Set<String> includedBoundedContextPackages) {
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
                includeFlowsTo,
                includedBoundedContextPackages
        );
    }

    /**
     * The packages the diagram is effectively restricted to, combining the explicitly included packages with the
     * included Bounded Contexts. If only one of them is set, it applies alone. If both are set, only what lies in
     * both remains: for each package and Bounded Context of which one lies in the other, the narrower one.
     *
     * @return the package prefixes the diagram's types must start with, empty for no restriction; {@link #NO_PACKAGE}
     * alone if package and Bounded Context filter exclude each other
     */
    public Set<String> getEffectiveIncludedPackages() {
        Set<String> packages = explicitlyIncludedPackagesNames == null ? Set.of() : explicitlyIncludedPackagesNames;
        Set<String> boundedContexts = includedBoundedContextPackages == null ? Set.of() : includedBoundedContextPackages;
        if (boundedContexts.isEmpty()) {
            return packages;
        }
        if (packages.isEmpty()) {
            return boundedContexts;
        }
        Set<String> effective = new HashSet<>();
        for (String packageName : packages) {
            for (String boundedContext : boundedContexts) {
                if (packageName.startsWith(boundedContext)) {
                    effective.add(packageName);
                } else if (boundedContext.startsWith(packageName)) {
                    effective.add(boundedContext);
                }
            }
        }
        return effective.isEmpty() ? Set.of(NO_PACKAGE) : effective;
    }
}
