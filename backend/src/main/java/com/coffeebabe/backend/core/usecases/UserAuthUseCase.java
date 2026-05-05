package com.coffeebabe.backend.core.usecases;

import com.coffeebabe.backend.core.entities.User;

public interface UserAuthUseCase {
    void signUp(String username, String email, String password);
    String signIn(String email, String password);
}
