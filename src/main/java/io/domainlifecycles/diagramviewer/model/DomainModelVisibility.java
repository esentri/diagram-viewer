package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class DomainModelVisibility {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long domainModelVisibilityId;

    @ElementCollection
    private List<String> seedClassNames;

    @ElementCollection
    private List<String> blacklistedClassNames;

    public DomainModelVisibility() {
    }

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
}
