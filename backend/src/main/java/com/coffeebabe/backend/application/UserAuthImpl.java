package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;
import com.coffeebabe.backend.core.exceptions.UserAlreadyExistsException;
import com.coffeebabe.backend.core.exceptions.UserNotAuthorizedException;
import com.coffeebabe.backend.core.gateways.PasswordGateway;
import com.coffeebabe.backend.core.gateways.TokenGateway;
import com.coffeebabe.backend.core.gateways.UserCrudGateway;
import com.coffeebabe.backend.core.usecases.UserAuthUseCase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserAuthImpl implements UserAuthUseCase {
    private final UserCrudGateway userCrudGateway;
    private final PasswordGateway passwordGateway;
    private final TokenGateway tokenGateway;

    public UserAuthImpl(UserCrudGateway userCrudGateway, PasswordGateway passwordGateway, TokenGateway tokenGateway) {
        this.userCrudGateway = userCrudGateway;
        this.passwordGateway = passwordGateway;
        this.tokenGateway = tokenGateway;
    }

    @Override
    public void signUp(String username, String email, String password) {
        var normalizedEmail = normalize(email);

        if (userCrudGateway.findUserByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException("UserAlreadyExistsException");
        }

        try {
            userCrudGateway.saveUser(new User(username, normalizedEmail, passwordGateway.hash(password), UserRole.USER));
        } catch (DataIntegrityViolationException e) {
            // username taken, or a concurrent sign-up with the same email
            throw new UserAlreadyExistsException("UserAlreadyExistsException");
        }
    }

    @Override
    public String signIn(String email, String password) {
        // same exception for unknown email and wrong password (SEC-6)
        var user = userCrudGateway.findUserByEmail(normalize(email))
                .filter(found -> passwordGateway.matches(password, found.getPassword()))
                .orElseThrow(() -> new UserNotAuthorizedException("UserNotAuthorizedException"));

        return tokenGateway.generateToken(user);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
