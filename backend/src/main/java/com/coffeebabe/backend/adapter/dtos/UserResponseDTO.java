package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;

public record UserResponseDTO(String username, String email, UserRole role) {
    public UserResponseDTO(User user) {
        this (user.getUsername(), user.getEmail(), user.getRole());
    }
}
