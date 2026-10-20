package com.coffeebabe.backend.adapter.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDTO(
        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank @Email @Size(max = 254) String email,
        // BCrypt only uses the first 72 bytes
        @NotBlank @Size(min = 8, max = 72) String password) {
}
