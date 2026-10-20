package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequestDTO(@NotNull OrderStatus status) {
}
