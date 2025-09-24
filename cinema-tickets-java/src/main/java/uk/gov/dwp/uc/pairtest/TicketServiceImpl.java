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

        int totalAmount = calculateTotalAmount(ticketTypeRequests);
        int totalSeats = calculateSeats(ticketTypeRequests);

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
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

    private int calculateTotalAmount(TicketTypeRequest... requests) {
        int total = 0;
        for (TicketTypeRequest req : requests) {
            switch (req.getTicketType()) {
                case ADULT -> total += req.getNoOfTickets() * 25;
                case CHILD -> total += req.getNoOfTickets() * 15;
                case INFANT -> total += 0;
            }
        }
        return total;
    }

    private int calculateSeats(TicketTypeRequest... requests) {
        int seats = 0;
        for (TicketTypeRequest req : requests) {
            if (req.getTicketType() != TicketTypeRequest.Type.INFANT) {
                seats += req.getNoOfTickets();
            }
        }
        return seats;
    }

}

