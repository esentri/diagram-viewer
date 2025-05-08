package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "DomainModelVisibility")
@AllArgsConstructor
@NoArgsConstructor
public class DomainModelVisibility {

    @Id
    @GeneratedValue
    private UUID id;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> filteredPackageNames;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> seedClassNames;

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
            Set<String> filteredPackageNames,
            Set<String> seedClassNames,
            Set<String> blacklistedClassNames
    ) {
        this.filteredPackageNames = filteredPackageNames == null ? new HashSet<>() : filteredPackageNames;
        this.seedClassNames = seedClassNames == null ? new HashSet<>() : seedClassNames;
        this.blacklistedClassNames = blacklistedClassNames == null ? new HashSet<>() : blacklistedClassNames;
    }

    public DomainModelVisibility replaceBlacklistedClassNames(Set<String> blacklistedClassNames) {
        return new DomainModelVisibility(filteredPackageNames, seedClassNames, blacklistedClassNames);
    }

    public DomainModelVisibility replaceSeedClassNames(Set<String> seedClassNames) {
        return new DomainModelVisibility(filteredPackageNames, seedClassNames, blacklistedClassNames);
    }

    public DomainModelVisibility replaceFilteredPackageNames(Set<String> filteredPackageNames) {
        return new DomainModelVisibility(filteredPackageNames, seedClassNames, blacklistedClassNames);
    }

}
