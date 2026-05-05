package com.coffeebabe.backend.adapter.dtos;

public record SignUpRequestDTO(String username, String email, String password) {
    public SignUpRequestDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
