package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.exceptions.InvalidDataException;
import com.coffeebabe.backend.core.exceptions.UserAlreadyExistsException;
import com.coffeebabe.backend.core.gateways.UserCrudGateway;
import com.coffeebabe.backend.core.usecases.UserCrudUseCase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.util.UUID;

@Service
public class UserCrudImpl implements UserCrudUseCase {
    private final UserCrudGateway userCrudGateway;

    public UserCrudImpl(UserCrudGateway userCrudGateway) {
        this.userCrudGateway = userCrudGateway;
    }

    @Override
    public User getUserById(UUID userId) {
        return userCrudGateway.getUserById(userId);
    }

    @Override
    public void removeUserById(UUID userId) {
        userCrudGateway.deleteUserById(userId);
    }

    @Override
    public void createUser(User user) {
        try {
            userCrudGateway.saveUser(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("UserAlreadyExistsException");
        } catch (TransactionSystemException e) {
            throw new InvalidDataException("InvalidDataException");
        }
    }
}
