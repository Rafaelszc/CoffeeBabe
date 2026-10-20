package com.coffeebabe.backend.core.entities;

// PENDING -> CONFIRMED -> READY -> (OUT_FOR_DELIVERY, delivery only) -> COMPLETED; CANCELLED from PENDING or CONFIRMED
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    READY,
    OUT_FOR_DELIVERY,
    COMPLETED,
    CANCELLED;

    public boolean canMoveTo(OrderStatus next, FulfillmentType fulfillment) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == READY || next == CANCELLED;
            case READY -> next == (fulfillment == FulfillmentType.DELIVERY ? OUT_FOR_DELIVERY : COMPLETED);
            case OUT_FOR_DELIVERY -> next == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
