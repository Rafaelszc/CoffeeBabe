package com.coffeebabe.backend.core.usecases;

import com.coffeebabe.backend.core.entities.AuthTokens;

public interface UserAuthUseCase {
    void signUp(String username, String email, String password);
    AuthTokens signIn(String email, String password);
    AuthTokens refresh(String refreshToken);
    void signOut(String refreshToken);
}
