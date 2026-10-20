package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.FulfillmentType;
import com.coffeebabe.backend.core.entities.Order;
import com.coffeebabe.backend.core.entities.OrderItem;
import com.coffeebabe.backend.core.entities.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(UUID id, OrderStatus status, FulfillmentType fulfillment, String address,
                               List<Item> items, BigDecimal subtotal, BigDecimal tax, BigDecimal deliveryFee,
                               BigDecimal total, Instant createdAt) {
    public record Item(String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        Item(OrderItem item) {
            this(item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.lineTotal());
        }
    }

    public OrderResponseDTO(Order order) {
        this(order.getId(),
                order.getStatus(),
                order.getFulfillment(),
                order.getAddress(),
                order.getItems().stream().map(Item::new).toList(),
                order.getSubtotal(),
                order.getTax(),
                order.getDeliveryFee(),
                order.getTotal(),
                order.getCreatedAt());
    }
}
