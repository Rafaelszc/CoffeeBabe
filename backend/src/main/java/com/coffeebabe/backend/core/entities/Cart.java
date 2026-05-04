package com.coffeebabe.backend.core.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    private UUID productId;

    public Cart(UUID userId, UUID productId) {
        this.userId = userId;
        this.productId = productId;
    }
}
