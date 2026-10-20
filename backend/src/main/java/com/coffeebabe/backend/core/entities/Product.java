package com.coffeebabe.backend.core.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "product")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 60)
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    public Product(String name, String description, BigDecimal price, ProductCategory category) {
        update(name, description, price, category);
    }

    public void update(String name, String description, BigDecimal price, ProductCategory category) {
        this.name = name;
        this.description = description;
        this.price = price == null ? null : price.setScale(2, RoundingMode.HALF_UP);
        this.category = category;
    }
}
