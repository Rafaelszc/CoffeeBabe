package com.coffeebabe.backend.adapter.dtos;

public record ProductRequestDTO(String name, String imageUrl, String description, double price) {
    public ProductRequestDTO(String name, String imageUrl, String description, double price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.price = price;
    }
}
