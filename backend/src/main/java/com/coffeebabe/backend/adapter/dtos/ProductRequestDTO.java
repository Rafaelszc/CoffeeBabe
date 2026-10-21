package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.ProductCategory;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank @Size(max = 60) String name,
        @NotBlank @Size(max = 1000) String description,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull ProductCategory category) {
}
