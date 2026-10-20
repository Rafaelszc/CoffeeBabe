package com.coffeebabe.backend.adapter.dtos;

import jakarta.validation.constraints.NotBlank;

public record SignInRequestDTO(@NotBlank String email, @NotBlank String password) {
}
