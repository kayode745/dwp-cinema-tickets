package uk.gov.dwp.uc.pairtest;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS = 25;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(
            TicketPaymentService ticketPaymentService,
            SeatReservationService seatReservationService) {

        this.ticketPaymentService = Objects.requireNonNull(ticketPaymentService);
        this.seatReservationService = Objects.requireNonNull(seatReservationService);
    }
    /**
     * Validates the account and ticket requests,
     * calculates the payment and required seats,
     * then processes the purchase.
     */
    @Override
    public void purchaseTickets(
            Long accountId,
            TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        // Check that the account ID is valid.
        validateAccountId(accountId);
        // Validate requests and group tickets by type.
        Map<Type, Integer> ticketCounts = countTicketsByType(ticketTypeRequests);

        // Ensure child and infant tickets have an accompanying adult.
        validateBusinessRules(ticketCounts);
        // Calculate the total payment and seats required.
        int totalAmount = calculateTotalAmount(ticketCounts);
        int totalSeats = calculateTotalSeats(ticketCounts);
        // Process payment before reserving seats.
        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }
    /**
     * Ensures the account ID is valid.
     */
    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }
    }
    /**
     * Validates ticket requests and groups quantities
     * by ticket type.
     */
    private Map<Type, Integer> countTicketsByType(
            TicketTypeRequest... ticketTypeRequests) {
        // Ensure at least one ticket request is provided.
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }
        // Store ticket quantities grouped by ticket type.
        Map<Type, Integer> ticketCounts = new EnumMap<>(Type.class);
        int totalTickets = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            // Reject null ticket requests.
            if (request == null) {
                throw new InvalidPurchaseException();
            }

            int quantity = request.getNoOfTickets();
            // Ensure ticket quantity is greater than zero.
            if (quantity <= 0) {
                throw new InvalidPurchaseException();
            }
            // Enforce the maximum ticket purchase limit.
            if (quantity > MAX_TICKETS - totalTickets) {
                throw new InvalidPurchaseException();
            }

            totalTickets += quantity;
            // Add quantities together for matching ticket types.
            ticketCounts.merge(
                    request.getTicketType(),
                    quantity,
                    Integer::sum
            );
        }

        return ticketCounts;
    }
    /**
     * Checks that child and infant tickets
     * are accompanied by an adult ticket.
     */
    private void validateBusinessRules(Map<Type, Integer> ticketCounts) {

        // Get the quantity of each ticket type.
        int adults = ticketCounts.getOrDefault(Type.ADULT, 0);
        int children = ticketCounts.getOrDefault(Type.CHILD, 0);
        int infants = ticketCounts.getOrDefault(Type.INFANT, 0);

        // Reject purchases containing children or infants without an adult.
        if (adults == 0 && (children > 0 || infants > 0)) {
            throw new InvalidPurchaseException();
        }
    }
    /**
     * Calculates the total payment using
     * the price assigned to each ticket type.
     */
    private int calculateTotalAmount(Map<Type, Integer> ticketCounts) {

        int totalAmount = 0;
        // Multiply each ticket type's price by its quantity.
        for (Map.Entry<Type, Integer> entry : ticketCounts.entrySet()) {
            totalAmount += entry.getKey().getPrice() * entry.getValue();
        }

        return totalAmount;
    }
    /**
     * Calculates the number of seats required,
     * excluding infant tickets.
     */
    private int calculateTotalSeats(Map<Type, Integer> ticketCounts) {

        int totalSeats = 0;
        // Only allocate seats for ticket types that require a seat.
        for (Map.Entry<Type, Integer> entry : ticketCounts.entrySet()) {

            if (entry.getKey().isAllocatedSeat()) {
                totalSeats += entry.getValue();
            }
        }

        return totalSeats;
    }
}
