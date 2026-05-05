package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;

public record SignInResponseDTO(String token) {
    public SignInResponseDTO(String token) {
        this.token = token;
    }
}
