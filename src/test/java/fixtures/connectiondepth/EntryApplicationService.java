package fixtures.connectiondepth;

import io.domainlifecycles.domain.types.ApplicationService;

public class EntryApplicationService implements ApplicationService {

    private final MiddleDomainService middleDomainService;

    public EntryApplicationService(MiddleDomainService middleDomainService) {
        this.middleDomainService = middleDomainService;
    }

    public void handle() {
        middleDomainService.handle();
    }
}
