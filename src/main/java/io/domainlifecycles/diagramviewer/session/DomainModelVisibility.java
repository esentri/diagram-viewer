package io.domainlifecycles.diagramviewer.session;

import java.util.ArrayList;
import java.util.List;

public record DomainModelVisibility(
        List<String> seedClassNames,
        List<String> blacklistedClassNames
) {
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
}
