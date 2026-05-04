package com.coffeebabe.backend.core.entities;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;;

    private String name;

    private String image_url;

    private String description;

    private double price;

    public Product(String name, String image_url, String description, double price) {
        this.name = name;
        this.image_url = image_url;
        this.description = description;
        this.price = price;
    }
}
