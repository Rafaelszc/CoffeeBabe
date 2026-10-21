package com.coffeebabe.backend.core.entities;

import com.coffeebabe.backend.core.exceptions.OrderStatusConflictException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FulfillmentType fulfillment;

    private String address;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Column(updatable = false)
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> items;

    public Order(UUID userId, FulfillmentType fulfillment, String address, List<CartItem> cartItems, PriceSummary prices) {
        this.userId = userId;
        this.fulfillment = fulfillment;
        this.address = address;
        this.status = OrderStatus.PENDING;
        this.subtotal = prices.subtotal();
        this.tax = prices.tax();
        this.deliveryFee = prices.deliveryFee();
        this.total = prices.total();
        this.createdAt = Instant.now();
        this.items = new ArrayList<>(cartItems.stream().map(OrderItem::new).toList());
    }

    public void moveTo(OrderStatus next) {
        if (!status.canMoveTo(next, fulfillment)) {
            throw new OrderStatusConflictException("OrderStatusConflictException");
        }
        this.status = next;
    }
}
