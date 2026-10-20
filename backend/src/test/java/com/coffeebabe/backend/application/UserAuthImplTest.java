package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;
import com.coffeebabe.backend.core.exceptions.UserAlreadyExistsException;
import com.coffeebabe.backend.core.exceptions.InvalidSessionException;
import com.coffeebabe.backend.core.exceptions.UserNotAuthorizedException;
import com.coffeebabe.backend.core.exceptions.UserNotFoundException;
import com.coffeebabe.backend.core.gateways.PasswordGateway;
import com.coffeebabe.backend.core.gateways.RefreshTokenGateway;
import com.coffeebabe.backend.core.gateways.RefreshTokenGateway.UsedToken;
import com.coffeebabe.backend.core.gateways.TokenGateway;
import com.coffeebabe.backend.core.gateways.UserCrudGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserAuthImplTest {
    private UserCrudGateway userCrudGateway;
    private PasswordGateway passwordGateway;
    private TokenGateway tokenGateway;
    private RefreshTokenGateway refreshTokens;
    private UserAuthImpl userAuth;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userCrudGateway = mock(UserCrudGateway.class);
        passwordGateway = mock(PasswordGateway.class);
        tokenGateway = mock(TokenGateway.class);
        refreshTokens = mock(RefreshTokenGateway.class);
        userAuth = new UserAuthImpl(userCrudGateway, passwordGateway, tokenGateway, refreshTokens, 7, 30);
    }

    @Test
    void signUpSavesHashedUserWithLowercaseEmail() {
        when(userCrudGateway.findUserByEmail("ana@mail.com")).thenReturn(Optional.empty());
        when(passwordGateway.hash("senha-forte")).thenReturn("hash");

        userAuth.signUp("ana", " Ana@Mail.com ", "senha-forte");

        var saved = ArgumentCaptor.forClass(User.class);
        verify(userCrudGateway).saveUser(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ana@mail.com");
        assertThat(saved.getValue().getPassword()).isEqualTo("hash");
        assertThat(saved.getValue().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void signUpRejectsExistingEmail() {
        when(userCrudGateway.findUserByEmail("ana@mail.com")).thenReturn(Optional.of(user()));

        assertThatThrownBy(() -> userAuth.signUp("ana", "ANA@mail.com", "senha-forte"))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(userCrudGateway, never()).saveUser(any());
    }

    @Test
    void signUpRejectsTakenUsername() {
        when(userCrudGateway.findUserByEmail(any())).thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("username")).when(userCrudGateway).saveUser(any());

        assertThatThrownBy(() -> userAuth.signUp("ana", "outra@mail.com", "senha-forte"))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void signInReturnsAccessTokenAndStoresOnlyTheRefreshHash() {
        var user = user();
        when(userCrudGateway.findUserByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(passwordGateway.matches("senha-forte", "hash")).thenReturn(true);
        when(tokenGateway.generateToken(user)).thenReturn("token");

        var tokens = userAuth.signIn("Ana@mail.com", "senha-forte");

        assertThat(tokens.accessToken()).isEqualTo("token");
        assertThat(tokens.refreshToken()).hasSize(43); // 32 random bytes, base64url without padding
        var hash = ArgumentCaptor.forClass(String.class);
        verify(refreshTokens).store(hash.capture(), eq(userId), eq(Duration.ofDays(7)));
        assertThat(hash.getValue()).isEqualTo(sha256(tokens.refreshToken())).isNotEqualTo(tokens.refreshToken());
    }

    @Test
    void refreshSpendsTheTokenAndIssuesANewPairWithTheCurrentRole() {
        var admin = new User(userId, "ana", "ana@mail.com", "hash", UserRole.ADMIN);
        when(refreshTokens.consume(sha256("old"), Duration.ofDays(7))).thenReturn(Optional.of(userId));
        when(userCrudGateway.getUserById(userId)).thenReturn(admin);
        when(tokenGateway.generateToken(admin)).thenReturn("admin-token");

        var tokens = userAuth.refresh("old");

        assertThat(tokens.accessToken()).isEqualTo("admin-token");
        assertThat(tokens.refreshToken()).isNotEqualTo("old");
        verify(refreshTokens).store(eq(sha256(tokens.refreshToken())), eq(userId), any());
    }

    @Test
    void unknownOrMissingRefreshTokenIsAnInvalidSession() {
        when(refreshTokens.consume(any(), any())).thenReturn(Optional.empty());
        when(refreshTokens.findUsed(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAuth.refresh("nope")).isInstanceOf(InvalidSessionException.class);
        assertThatThrownBy(() -> userAuth.refresh(null)).isInstanceOf(InvalidSessionException.class);
        verify(refreshTokens, never()).revokeAll(any());
    }

    @Test
    void reusingASpentTokenRevokesEverySession() {
        when(refreshTokens.consume(any(), any())).thenReturn(Optional.empty());
        when(refreshTokens.findUsed(sha256("stolen")))
                .thenReturn(Optional.of(new UsedToken(userId, Instant.now().minusSeconds(60))));

        assertThatThrownBy(() -> userAuth.refresh("stolen")).isInstanceOf(InvalidSessionException.class);
        verify(refreshTokens).revokeAll(userId);
    }

    @Test
    void reuseRightAfterRotationIsNotTreatedAsTheft() {
        when(refreshTokens.consume(any(), any())).thenReturn(Optional.empty());
        when(refreshTokens.findUsed(sha256("two-tabs")))
                .thenReturn(Optional.of(new UsedToken(userId, Instant.now().minusSeconds(5))));

        assertThatThrownBy(() -> userAuth.refresh("two-tabs")).isInstanceOf(InvalidSessionException.class);
        verify(refreshTokens, never()).revokeAll(any());
    }

    @Test
    void refreshForADeletedUserIsAnInvalidSession() {
        when(refreshTokens.consume(any(), any())).thenReturn(Optional.of(userId));
        when(userCrudGateway.getUserById(userId)).thenThrow(new UserNotFoundException("x"));

        assertThatThrownBy(() -> userAuth.refresh("old")).isInstanceOf(InvalidSessionException.class);
    }

    @Test
    void signOutRevokesOnlyThePresentedToken() {
        userAuth.signOut("mine");
        userAuth.signOut(null);

        verify(refreshTokens).revoke(sha256("mine"));
        verifyNoMoreInteractions(refreshTokens);
    }

    @Test
    void signInFailsTheSameWayForUnknownEmailAndWrongPassword() {
        when(userCrudGateway.findUserByEmail("nobody@mail.com")).thenReturn(Optional.empty());
        when(userCrudGateway.findUserByEmail("ana@mail.com")).thenReturn(Optional.of(user()));
        when(passwordGateway.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> userAuth.signIn("nobody@mail.com", "senha-forte"))
                .isInstanceOf(UserNotAuthorizedException.class);
        assertThatThrownBy(() -> userAuth.signIn("ana@mail.com", "errada"))
                .isInstanceOf(UserNotAuthorizedException.class);
        verifyNoInteractions(tokenGateway);
    }

    private User user() {
        return new User(userId, "ana", "ana@mail.com", "hash", UserRole.USER);
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
