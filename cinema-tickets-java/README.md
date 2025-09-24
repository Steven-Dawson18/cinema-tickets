# Cinema Tickets Reservation Service

## Overview
Implement the TicketService to purchase cinema tickets correctly following the business rules.
Validation of requests, calculate totals and use external payment and seat reservation services.

## Business Rules
- Adult: £25
- Child: £15
- Infant: £0 (no seat)
- Max 25 tickets per purchase
- Child/Infant tickets require at least one Adult
- Throws InvalidPurchaseException for invalid scenarios

## Assumptions
- All accounts with an ID greater than zero are valid and have sufficient funds.
- External services (TicketPaymentService, SeatReservationService) are reliable and defect-free.
- There cannot be more Infants than Adults, as they sit on an adults knee and don't get allocated a seat.

## Technologies used
- Java 21
- JUnit 5
- Mockito
- Maven

## Implementation
- The main implementation is in:
  src/main/java/uk/gov/dwp/uc/pairtest/TicketServiceImpl.java
- The class is structured with:
- Validation methods for input and business rules.
- Calculation methods for totals and seat counts

## Tests
- Unit tests are in:
  src/test/java/uk/gov/dwp/uc/pairtest/TicketServiceImplTest.java
- Tests cover:
- Invalid scenarios (e.g. null account, no tickets, exceeding max tickets).
- Valid purchase flows (single adult, mixed adult/child/infant).
- Interaction verification with external services (Mockito).

## How to run
- Compile and run tests:
  mvn clean test

## Design and approach
- Followed TDD: each rule was added by writing a failing test first.
- Kept code clean, readable, and modular.
- Documented assumptions and edge cases in this README.

## Next steps if extended
- Creation of helper functions to improve flow and readability.
- Add logging for audit trails.