package com.coffeebabe.backend.adapter.dtos;

import java.util.UUID;

public record CartRequestDTO(UUID productId, UUID userId) {
    public CartRequestDTO(UUID productId, UUID userId) {
        this.productId = productId;
        this.userId = userId;
    }
}
