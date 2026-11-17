package uk.gov.dwp.uc.pairtest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TicketServiceImplTest {

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldPurchaseAdultTicket() {
        // Arrange
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);

        // Act
        ticketService.purchaseTickets(1L, adult);

        // Assert
        verify(paymentService).makePayment(1L, 25);
        verify(seatReservationService).reserveSeat(1L, 1);
    }

    @Test
    void shouldPurchaseAdultAndChildTickets() {
        // Arrange
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 1);

        // Act
        ticketService.purchaseTickets(1L, adult, child);

        // Assert
        verify(paymentService).makePayment(1L, 40);
        verify(seatReservationService).reserveSeat(1L, 2);
    }

    @Test
    void shouldNotChargeForInfantOrReserveSeat() {
        // Arrange
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);
        TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 1);

        // Act
        ticketService.purchaseTickets(1L, adult, infant);

        // Assert
        verify(paymentService).makePayment(1L, 25);
        verify(seatReservationService).reserveSeat(1L, 1);
    }

    @Test
    void shouldCalculatePaymentAndSeatsForMultipleTickets() {
        // Arrange
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 1);

        // Act
        ticketService.purchaseTickets(1L, adult, child, infant);

        // Assert
        verify(paymentService).makePayment(1L, 65);
        verify(seatReservationService).reserveSeat(1L, 3);
    }

    @Test
    void shouldAllowExactly25Tickets() {
        // Arrange
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 10);
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 10);
        TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 5);

        // Act
        ticketService.purchaseTickets(1L, adult, child, infant);

        // Assert
        verify(paymentService).makePayment(1L, 400);
        verify(seatReservationService).reserveSeat(1L, 20);
    }

    @Test
    void shouldRejectMoreThan25Tickets() {
        // Arrange
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 26);

        // Act & Assert
        assertRejected(1L, adult);
    }

    @Test
    void shouldRejectChildOrInfantWithoutAdult() {
        // Arrange
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 1);

        // Act & Assert
        assertRejected(1L, child);
        assertRejected(1L, infant);
    }

    @Test
    void shouldRejectInvalidAccountAndTicketRequests() {
        // Arrange
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);
        TicketTypeRequest zeroQuantity = new TicketTypeRequest(Type.ADULT, 0);
        TicketTypeRequest negativeQuantity = new TicketTypeRequest(Type.ADULT, -1);

        // Act & Assert
        assertRejected(0L, adult);
        assertRejected(-1L, adult);
        assertRejected(1L, zeroQuantity);
        assertRejected(1L, negativeQuantity);
        assertRejected(1L);
    }

    private void assertRejected(
            Long accountId,
            TicketTypeRequest... requests) {

        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(accountId, requests)
        );

        verifyNoInteractions(paymentService, seatReservationService);
    }
}

