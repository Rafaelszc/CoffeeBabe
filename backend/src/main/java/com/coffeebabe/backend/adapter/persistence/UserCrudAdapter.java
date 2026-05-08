package com.coffeebabe.backend.adapter.persistence;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.exceptions.UserNotFoundException;
import com.coffeebabe.backend.core.gateways.UserCrudGateway;
import com.coffeebabe.backend.infra.persistence.jpa.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserCrudAdapter implements UserCrudGateway {
    private final UserRepository userRepository;

    public UserCrudAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("UserNotFoundException"));
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUserById(UUID userId) {
        userRepository.deleteById(userId);
    }
}
