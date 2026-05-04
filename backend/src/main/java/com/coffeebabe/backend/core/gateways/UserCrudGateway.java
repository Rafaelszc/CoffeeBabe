package com.coffeebabe.backend.core.gateways;

import com.coffeebabe.backend.core.entities.User;

import java.util.UUID;

public interface UserCrudGateway {
    User getUserById(UUID userId);
    void saveUser(User user);
    void deleteUserById(UUID userId);
}
