package com.coffeebabe.backend.core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    private UUID userId;

    @NotBlank
    private UUID productId;

    public Cart(UUID userId, UUID productId) {
        this.userId = userId;
        this.productId = productId;
    }
}
