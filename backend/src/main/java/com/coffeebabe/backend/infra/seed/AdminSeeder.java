package com.coffeebabe.backend.infra.seed;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;
import com.coffeebabe.backend.core.gateways.PasswordGateway;
import com.coffeebabe.backend.core.gateways.UserCrudGateway;
import com.coffeebabe.backend.core.usecases.UserCrudUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;

// Creates the first ADMIN from app.admin.* (ADMIN_EMAIL / ADMIN_PASSWORD); sign-up only creates USER (RF-ID-5)
@Component
public class AdminSeeder implements ApplicationRunner {
    private final UserCrudUseCase userCrudUseCase;
    private final UserCrudGateway userCrudGateway;
    private final PasswordGateway passwordGateway;
    private final String email;
    private final String password;

    public AdminSeeder(UserCrudUseCase userCrudUseCase,
                       UserCrudGateway userCrudGateway,
                       PasswordGateway passwordGateway,
                       @Value("${app.admin.email:}") String email,
                       @Value("${app.admin.password:}") String password) {
        this.userCrudUseCase = userCrudUseCase;
        this.userCrudGateway = userCrudGateway;
        this.passwordGateway = passwordGateway;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank() || userCrudGateway.findUserByEmail(email).isPresent()) {
            return;
        }

        userCrudUseCase.createUser(new User("admin", email, passwordGateway.hash(password), UserRole.ADMIN));
    }
}
