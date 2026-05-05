package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.gateways.UserCrudGateway;
import com.coffeebabe.backend.core.usecases.UserAuthUseCase;
import org.springframework.stereotype.Service;

@Service
public class UserAuthImpl implements UserAuthUseCase {
    private final UserCrudGateway userCrudGateway;

    public UserAuthImpl(UserCrudGateway userCrudGateway) {
        this.userCrudGateway = userCrudGateway;
    }

    @Override
    public void signUp(String username, String email, String password) {

    }

    @Override
    public String signIn(String email, String password) {
        throw new RuntimeException();
    }
}
