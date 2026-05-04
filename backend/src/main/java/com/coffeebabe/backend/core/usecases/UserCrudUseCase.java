package com.coffeebabe.backend.core.usecases;

import com.coffeebabe.backend.core.entities.User;

import java.util.UUID;

public interface UserCrudUseCase {
    User getUserById(UUID userId);
    void removeUserById(UUID userId);
    void createUser(User user);
}
