package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.Product;

import java.util.UUID;

public record ProductResponseDTO(UUID id, String name, String imageUrl, String description, double price) {
    public ProductResponseDTO(Product product) {
        this(product.getId(),
                product.getName(),
                product.getImageUrl(),
                product.getDescription(),
                product.getPrice());
    }
}
