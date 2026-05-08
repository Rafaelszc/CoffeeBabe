package com.coffeebabe.backend.core.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String imageUrl;

    @NotBlank
    private String description;

    @NotBlank
    private double price;

    public Product(String name, String imageUrl, String description, double price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.price = price;
    }
}
