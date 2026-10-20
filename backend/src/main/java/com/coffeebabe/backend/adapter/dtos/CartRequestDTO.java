package com.coffeebabe.backend.adapter.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CartRequestDTO(@NotNull UUID productId) {
}
