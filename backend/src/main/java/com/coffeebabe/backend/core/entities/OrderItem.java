package com.coffeebabe.backend.core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // null once the product is deleted; the name snapshot stays
    private UUID productId;

    @NotBlank
    private String productName;

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Positive
    private int quantity;

    public OrderItem(CartItem item) {
        this.productId = item.product().getId();
        this.productName = item.product().getName();
        this.unitPrice = item.product().getPrice();
        this.quantity = item.quantity();
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
