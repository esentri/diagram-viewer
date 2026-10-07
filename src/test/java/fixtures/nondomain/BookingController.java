package fixtures.nondomain;

/**
 * A non-domain class (no DLC marker interface) that itself references an ApplicationService,
 * like a REST controller calling into the application layer.
 */
public class BookingController {

    private final BookingApplicationService bookingApplicationService;

    public BookingController(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    public String postBooking(String guest) {
        return bookingApplicationService.book(guest);
    }
}
