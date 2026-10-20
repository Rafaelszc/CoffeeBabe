package com.coffeebabe.backend.core.gateways;

public interface PasswordGateway {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
