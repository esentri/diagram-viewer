package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "DomainModelVisibility")
@AllArgsConstructor
@NoArgsConstructor
public class DomainModelVisibility {

    @Id
    @GeneratedValue
    private UUID id;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> seedClassNames;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> blacklistedClassNames;

    @CreationTimestamp
    private Instant createdAt;

    public DomainModelVisibility(List<String> seedClassNames, List<String> blacklistedClassNames) {
        this.seedClassNames = seedClassNames == null ? new ArrayList<>() : seedClassNames;
        this.blacklistedClassNames = blacklistedClassNames == null ? new ArrayList<>() : blacklistedClassNames;
    }

    public DomainModelVisibility replaceBlacklistedClassNames(List<String> blacklistedClassNames) {
        return new DomainModelVisibility(seedClassNames, blacklistedClassNames);
    }

    public DomainModelVisibility replaceSeedClassNames(List<String> seedClassNames) {
        return new DomainModelVisibility(seedClassNames, blacklistedClassNames);
    }

    public List<String> getSeedClassNames() {
        return seedClassNames;
    }

    public List<String> getBlacklistedClassNames() {
        return blacklistedClassNames;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
