package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.UserRole;

public record SignInRequestDTO(String email, String password) {
    public SignInRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
