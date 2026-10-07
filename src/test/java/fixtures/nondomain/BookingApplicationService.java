package fixtures.nondomain;

import io.domainlifecycles.domain.types.ApplicationService;

/**
 * An ApplicationService depending on a non-domain class ({@link BookingMapper}) via a field.
 */
public class BookingApplicationService implements ApplicationService {

    private final BookingMapper mapper;

    public BookingApplicationService(BookingMapper mapper) {
        this.mapper = mapper;
    }

    public String book(String guest) {
        return mapper.map(guest);
    }
}
