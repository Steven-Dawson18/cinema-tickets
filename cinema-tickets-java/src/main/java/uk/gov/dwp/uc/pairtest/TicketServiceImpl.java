package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId);
        validateTicketRequests(ticketTypeRequests);
    }

    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }
    }

    private void validateTicketRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }

        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        for (TicketTypeRequest req : ticketTypeRequests) {
            if (req == null || req.getTicketType() == null || req.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException();
            }

            switch (req.getTicketType()) {
                case ADULT -> adultCount += req.getNoOfTickets();
                case CHILD -> childCount += req.getNoOfTickets();
                case INFANT -> infantCount += req.getNoOfTickets();
            }
        }

        if (adultCount == 0 && (childCount > 0 || infantCount > 0)) {
            throw new InvalidPurchaseException();
        }
    }
}

