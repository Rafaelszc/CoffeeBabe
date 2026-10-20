package com.coffeebabe.backend.core.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "product_image")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductImage {
    @Id
    private UUID productId;

    @NotBlank
    private String contentType;

    @NotNull
    private byte[] data;

    // SHA-256 of data, served as the ETag
    @NotBlank
    private String etag;
}
