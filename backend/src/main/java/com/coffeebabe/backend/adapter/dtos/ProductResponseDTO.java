package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(UUID id, String name, String description, BigDecimal price,
                                 ProductCategory category, String imageUrl) {
    public ProductResponseDTO(Product product) {
        this(product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                "/api/product/" + product.getId() + "/image");
    }
}
