package uk.gov.dwp.uc.pairtest.domain;

import java.util.Objects;

/**
 * Immutable Object
 */

public final class TicketTypeRequest {

    private final int noOfTickets;
    private final Type type;

    public TicketTypeRequest(Type type, int noOfTickets) {
        this.type = Objects.requireNonNull(type, "Ticket type must not be null");
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public enum Type {
        ADULT (25, true),
        CHILD (15, true),
        INFANT (0,false);

        private final int price;
        private final boolean allocatedSeat;

        Type(int price, boolean allocatedSeat){
            this.price = price;
            this.allocatedSeat = allocatedSeat;
        }
        public int getPrice() {
            return price;
        }
        public boolean isAllocatedSeat(){
            return allocatedSeat;
        }
    }

}
