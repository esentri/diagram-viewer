package fixtures.nondomain;

/**
 * A non-domain class (no DLC marker interface) referenced by {@link BookingApplicationService}.
 */
public class BookingMapper {

    public String map(String guest) {
        return guest.trim();
    }
}
