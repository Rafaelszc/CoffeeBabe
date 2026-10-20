package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;
import com.coffeebabe.backend.core.exceptions.UserAlreadyExistsException;
import com.coffeebabe.backend.core.exceptions.UserNotAuthorizedException;
import com.coffeebabe.backend.core.gateways.PasswordGateway;
import com.coffeebabe.backend.core.gateways.TokenGateway;
import com.coffeebabe.backend.core.gateways.UserCrudGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserAuthImplTest {
    private UserCrudGateway userCrudGateway;
    private PasswordGateway passwordGateway;
    private TokenGateway tokenGateway;
    private UserAuthImpl userAuth;

    @BeforeEach
    void setUp() {
        userCrudGateway = mock(UserCrudGateway.class);
        passwordGateway = mock(PasswordGateway.class);
        tokenGateway = mock(TokenGateway.class);
        userAuth = new UserAuthImpl(userCrudGateway, passwordGateway, tokenGateway);
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
    void signInReturnsToken() {
        var user = user();
        when(userCrudGateway.findUserByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(passwordGateway.matches("senha-forte", "hash")).thenReturn(true);
        when(tokenGateway.generateToken(user)).thenReturn("token");

        assertThat(userAuth.signIn("Ana@mail.com", "senha-forte")).isEqualTo("token");
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

    private static User user() {
        return new User("ana", "ana@mail.com", "hash", UserRole.USER);
    }
}
