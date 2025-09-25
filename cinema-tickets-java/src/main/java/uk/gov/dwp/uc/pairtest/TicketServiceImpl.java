package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

/**
 * Implementation of TicketService that validates requests,
 * calculates totals, processes payments, and reserves seats.
 */
public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    // Improve maintainability with constants
    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;
    private static final int INFANT_TICKET_PRICE = 0;
    private static final int MAX_TICKETS = 25;
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    /**
     * Creates a new TicketServiceImpl.
     *
     * @param ticketPaymentService service used to process payments
     * @param seatReservationService service used to reserve seats
     * @throws NullPointerException if any dependency is null
     */
    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Purchases tickets.
     *
     * This method validates the request, calculates the total cost
     * and seats required, and then calls external payment
     * and seat reservation services.
     *
     * @param accountId the account making the purchase
     * @param ticketTypeRequests one or more TicketTypeRequests
     * @throws InvalidPurchaseException if the request violates business rules
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId);
        validateTicketRequests(ticketTypeRequests);

        int totalAmount = calculateTotalAmount(ticketTypeRequests);
        int totalSeats = calculateSeats(ticketTypeRequests);

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    /**
     * Validates the account ID.
     *
     * @param accountId the account ID to validate
     * @throws InvalidPurchaseException if is null, zero, or negative
     */
    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }
    }

    /**
     * Validates the given ticket requests against business rules.
     *
     * This method ensures that the request is not null or empty,
     * that each request is valid, and that the request complies with
     * rules around the business rules, must be an adult ticket,
     * maximum tickets, and infant seating.
     *
     * @param ticketTypeRequests the ticket requests to validate
     * @throws InvalidPurchaseException if the requests are invalid or violate business rules
     */
    private void validateTicketRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }

        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;
        int totalTickets = 0;

        for (TicketTypeRequest req : ticketTypeRequests) {
            if (req == null || req.getTicketType() == null || req.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException();
            }

            totalTickets += req.getNoOfTickets();
            switch (req.getTicketType()) {
                case ADULT -> adultCount += req.getNoOfTickets();
                case CHILD -> childCount += req.getNoOfTickets();
                case INFANT -> infantCount += req.getNoOfTickets();
            }
        }

        if (adultCount == 0 && (childCount > 0 || infantCount > 0)) {
            throw new InvalidPurchaseException();
        }

        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException();
        }

        if (infantCount > adultCount) {
            throw new InvalidPurchaseException();
        }

    }

    /**
     * Calculates the total cost of the requested tickets.
     * Adults cost £25 each, children cost £15 each, and infants are free.
     *
     * @param requests the ticket requests to process
     * @return the total payment amount
     */
    private int calculateTotalAmount(TicketTypeRequest... requests) {
        return Arrays.stream(requests)
                .mapToInt(req -> switch (req.getTicketType()) {
                    case ADULT -> req.getNoOfTickets() * ADULT_TICKET_PRICE;
                    case CHILD -> req.getNoOfTickets() * CHILD_TICKET_PRICE;
                    case INFANT -> INFANT_TICKET_PRICE;
                })
                .sum();
    }

    /**
     * Calculates how many seats need to be reserved.
     * Infants sit on an adults lap and do not need a seat,
     * all other tickets require one seat each.
     *
     * @param requests the ticket requests to process
     * @return the total number of seats to reserve
     */
    private int calculateSeats(TicketTypeRequest... requests) {
        return Arrays.stream(requests)
                .filter(req -> req.getTicketType() != TicketTypeRequest.Type.INFANT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

}

