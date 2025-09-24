package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("TicketServiceImplTest")
class TicketServiceImplTest {

    private TicketPaymentService paymentService;
    private SeatReservationService seatService;
    private TicketService ticketService;

    @BeforeEach
    void setup() {
        paymentService = mock(TicketPaymentService.class);
        seatService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, seatService);
    }

    @Test
    @DisplayName("Should throw exception when accountId is null, zero or negative")
    void shouldThrowWhenAccountIdIsInvalid() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(null, adult));

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, adult));

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(-5L, adult));
    }

    @Test
    @DisplayName("Should throw exception when no ticket requests are provided")
    void shouldThrowWhenNoTicketRequestsProvided() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L));

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null));
    }

    @Test
    @DisplayName("Should throw exception when child or infant tickets are purchased without an adult")
    void shouldThrowWhenNoAdultTicketsProvided() {
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, child));

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, infant));

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, child, infant));
    }

    @Test
    @DisplayName("Should process a single adult ticket for correct amount and seat")
    void shouldProcessSingleAdultTicket() {
        final long accountId = 1L;
        TicketTypeRequest oneAdult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        ticketService.purchaseTickets(accountId, oneAdult);

        verify(paymentService).makePayment(accountId, 25);
        verify(seatService).reserveSeat(accountId, 1);
    }

    @Test
    @DisplayName("Should process purchase with adult child and infant tickets")
    void shouldProcessAdultChildAndInfantTicketPurchase() {
        final long accountId = 2L;
        TicketTypeRequest adults = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest children = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        TicketTypeRequest infants = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(accountId, adults, children, infants);

        // 2 adults = 50, 3 children = 45, 1 infant = 0
        verify(paymentService).makePayment(accountId, 95);

        // infants don't get seats therefore 2 adults + 3 children = 5
        verify(seatService).reserveSeat(accountId, 5);
    }

    @Test
    @DisplayName("Should throw exception if more than 25 tickets")
    void shouldThrowWhenMoreThan25TicketsPurchased() {
        TicketTypeRequest adults = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
        TicketTypeRequest children = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 6);

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, adults, children));

        verifyNoInteractions(paymentService, seatService);
    }

}
