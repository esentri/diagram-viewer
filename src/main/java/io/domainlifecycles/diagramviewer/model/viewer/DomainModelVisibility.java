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
            Set<String> blacklistedClassNames
    ) {
        this.explicitlyIncludedPackagesNames = explicitlyIncludedPackagesNames == null ? new HashSet<>() : explicitlyIncludedPackagesNames;
        this.includeConnectedToIngoingClassNames = includeConnectedToIngoingClassNames == null ? new HashSet<>() : includeConnectedToIngoingClassNames;
        this.includeConnectedToOutgoingClassNames = includeConnectedToOutgoingClassNames == null ? new HashSet<>() : includeConnectedToOutgoingClassNames;
        this.excludeConnectedToIngoingClassNames = excludeConnectedToIngoingClassNames == null ? new HashSet<>() : excludeConnectedToIngoingClassNames;
        this.excludeConnectedToOutgoingClassNames = excludeConnectedToOutgoingClassNames == null ? new HashSet<>() : excludeConnectedToOutgoingClassNames;
        this.includeConnectedToClassNames = includeConnectedToClassNames == null ? new HashSet<>() : includeConnectedToClassNames;
        this.blacklistedClassNames = blacklistedClassNames == null ? new HashSet<>() : blacklistedClassNames;
    }

    public DomainModelVisibility replaceBlacklistedClassNames(Set<String> blacklistedClassNames) {
        return new DomainModelVisibility(
                explicitlyIncludedPackagesNames,
                includeConnectedToClassNames,
                includeConnectedToIngoingClassNames,
                includeConnectedToOutgoingClassNames,
                excludeConnectedToIngoingClassNames,
                excludeConnectedToOutgoingClassNames,
                blacklistedClassNames
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
                blacklistedClassNames
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
                blacklistedClassNames
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
                blacklistedClassNames
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
                blacklistedClassNames
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
                blacklistedClassNames
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
                blacklistedClassNames
        );
    }
}
