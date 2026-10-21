package com.coffeebabe.backend.core.entities;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static com.coffeebabe.backend.core.entities.FulfillmentType.DELIVERY;
import static com.coffeebabe.backend.core.entities.FulfillmentType.PICKUP;
import static com.coffeebabe.backend.core.entities.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {
    @Test
    void pickupTransitions() {
        assertThat(allowed(PENDING, PICKUP)).containsExactlyInAnyOrder(CONFIRMED, CANCELLED);
        assertThat(allowed(CONFIRMED, PICKUP)).containsExactlyInAnyOrder(READY, CANCELLED);
        assertThat(allowed(READY, PICKUP)).containsExactly(COMPLETED);
        assertThat(allowed(COMPLETED, PICKUP)).isEmpty();
        assertThat(allowed(CANCELLED, PICKUP)).isEmpty();
    }

    @Test
    void deliveryGoesOutForDeliveryBeforeCompleting() {
        assertThat(allowed(READY, DELIVERY)).containsExactly(OUT_FOR_DELIVERY);
        assertThat(allowed(OUT_FOR_DELIVERY, DELIVERY)).containsExactly(COMPLETED);
    }

    private static Set<OrderStatus> allowed(OrderStatus from, FulfillmentType fulfillment) {
        var result = EnumSet.noneOf(OrderStatus.class);
        for (var next : OrderStatus.values()) {
            if (from.canMoveTo(next, fulfillment)) result.add(next);
        }
        return result;
    }
}
