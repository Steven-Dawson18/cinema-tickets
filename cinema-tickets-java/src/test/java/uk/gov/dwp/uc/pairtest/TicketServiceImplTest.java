package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

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
}
