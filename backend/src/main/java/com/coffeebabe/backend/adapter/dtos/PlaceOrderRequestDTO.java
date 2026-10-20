package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.FulfillmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// no prices here on purpose: the server computes them (RF-ORD-4); address is required for DELIVERY
public record PlaceOrderRequestDTO(@NotNull FulfillmentType fulfillment, @Size(max = 300) String address) {
}
