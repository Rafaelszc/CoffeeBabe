package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.AuthTokens;
import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;
import com.coffeebabe.backend.core.exceptions.InvalidSessionException;
import com.coffeebabe.backend.core.exceptions.UserAlreadyExistsException;
import com.coffeebabe.backend.core.exceptions.UserNotAuthorizedException;
import com.coffeebabe.backend.core.exceptions.UserNotFoundException;
import com.coffeebabe.backend.core.gateways.PasswordGateway;
import com.coffeebabe.backend.core.gateways.RefreshTokenGateway;
import com.coffeebabe.backend.core.gateways.TokenGateway;
import com.coffeebabe.backend.core.gateways.UserCrudGateway;
import com.coffeebabe.backend.core.usecases.UserAuthUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class UserAuthImpl implements UserAuthUseCase {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserCrudGateway userCrudGateway;
    private final PasswordGateway passwordGateway;
    private final TokenGateway tokenGateway;
    private final RefreshTokenGateway refreshTokenGateway;
    private final Duration refreshTtl;
    private final Duration reuseGrace;

    public UserAuthImpl(UserCrudGateway userCrudGateway,
                        PasswordGateway passwordGateway,
                        TokenGateway tokenGateway,
                        RefreshTokenGateway refreshTokenGateway,
                        @Value("${security.refresh-token.ttl-days}") long refreshTtlDays,
                        @Value("${security.refresh-token.reuse-grace-seconds}") long reuseGraceSeconds) {
        this.userCrudGateway = userCrudGateway;
        this.passwordGateway = passwordGateway;
        this.tokenGateway = tokenGateway;
        this.refreshTokenGateway = refreshTokenGateway;
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
        this.reuseGrace = Duration.ofSeconds(reuseGraceSeconds);
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
    public AuthTokens signIn(String email, String password) {
        // same exception for unknown email and wrong password (SEC-6)
        var user = userCrudGateway.findUserByEmail(normalize(email))
                .filter(found -> passwordGateway.matches(password, found.getPassword()))
                .orElseThrow(() -> new UserNotAuthorizedException("UserNotAuthorizedException"));

        return issueTokens(user);
    }

    // Rotation: the presented token is spent and a new pair is issued (ADR-0008)
    @Override
    public AuthTokens refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidSessionException("InvalidSessionException");
        }

        var hash = sha256(refreshToken);
        var userId = refreshTokenGateway.consume(hash, refreshTtl);
        if (userId.isEmpty()) {
            // a spent token coming back means it leaked, unless two tabs just refreshed together
            refreshTokenGateway.findUsed(hash)
                    .filter(used -> used.usedAt().plus(reuseGrace).isBefore(Instant.now()))
                    .ifPresent(used -> refreshTokenGateway.revokeAll(used.userId()));
            throw new InvalidSessionException("InvalidSessionException");
        }

        try {
            // the role comes from the database, so a role change applies on the next refresh
            return issueTokens(userCrudGateway.getUserById(userId.get()));
        } catch (UserNotFoundException e) {
            throw new InvalidSessionException("InvalidSessionException");
        }
    }

    @Override
    public void signOut(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenGateway.revoke(sha256(refreshToken));
        }
    }

    private AuthTokens issueTokens(User user) {
        var bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        var refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        refreshTokenGateway.store(sha256(refreshToken), user.getId(), refreshTtl);
        return new AuthTokens(tokenGateway.generateToken(user), refreshToken);
    }

    private static String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
