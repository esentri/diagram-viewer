package fixtures.connectiondepth;

import io.domainlifecycles.domain.types.DomainService;

public class MiddleDomainService implements DomainService {

    private final LastDomainService lastDomainService;

    public MiddleDomainService(LastDomainService lastDomainService) {
        this.lastDomainService = lastDomainService;
    }

    public void handle() {
        lastDomainService.handle();
    }
}
