package com.coffeebabe.backend.adapter.security;

import com.coffeebabe.backend.core.gateways.PasswordGateway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordAdapter implements PasswordGateway {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
