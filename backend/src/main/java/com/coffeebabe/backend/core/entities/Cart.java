package com.coffeebabe.backend.core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private UUID userId;

    @NotNull
    private UUID productId;

    @Positive
    private int quantity;

    public Cart(UUID userId, UUID productId) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = 1;
    }

    public void increase() {
        quantity++;
    }

    public void decrease() {
        quantity--;
    }
}
