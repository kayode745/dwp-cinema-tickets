CINEMA TICKET SERVICE

OVERVIEW

TicketServiceImpl handles cinema ticket purchases by validating requests, calculating payment amounts and coordinating seat reservations through the provided external services.

MAIN METHODS

purchaseTickets: Validates requests, enforces business rules, calculates the total cost and required seats, and processes payments and reservations.

countTicketsByType: Groups ticket quantities by ticket type for easier processing.

validateBusinessRules: Checks that child and infant tickets are purchased with at least one adult ticket.

calculateTotalAmount: Calculates the total purchase cost based on ticket types.

calculateTotalSeats: Determines the number of seats required, excluding infants.

DESIGN APPROACH

The implementation uses constructor injection for its dependencies and separates validation, payment calculation and seat allocation into focused methods.

TicketTypeRequest is implemented as an immutable class, while ticket pricing and seat allocation are defined within the ticket type enum.

TESTING

TicketServiceImplTest uses JUnit 5 and Mockito to test the service independently of external payment and seat reservation providers.

KEY TEST CASES

- Successful adult and child ticket purchases.
- Infant pricing and seat allocation.
- Multiple ticket types and payment calculations.
- Maximum ticket limit enforcement.
- Invalid account IDs and ticket quantities.
- Child or infant purchases without an adult.

WHY IT WORKS

The tests verify both successful and rejected purchases, ensuring that the service behaves as expected and external services are only called for valid transactions.

