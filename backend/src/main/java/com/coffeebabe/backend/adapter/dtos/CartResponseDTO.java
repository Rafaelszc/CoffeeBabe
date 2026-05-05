package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.Product;

import java.util.List;

public record CartResponseDTO(List<Product> products) {
    public CartResponseDTO(List<Product> products) {
        this.products = products;
    }
    public CartResponseDTO(Product product) {
        this (List.of(product));
    }
}
