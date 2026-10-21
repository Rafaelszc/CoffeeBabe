package com.coffeebabe.backend.core.gateways;

import com.coffeebabe.backend.core.entities.User;

public interface TokenGateway {
    String generateToken(User user);
}
