package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.SignInRequestDTO;
import com.coffeebabe.backend.adapter.dtos.SignUpRequestDTO;
import com.coffeebabe.backend.adapter.dtos.UserResponseDTO;
import com.coffeebabe.backend.core.usecases.UserAuthUseCase;
import com.coffeebabe.backend.core.usecases.UserCrudUseCase;
import com.coffeebabe.backend.infra.security.SecurityConfig;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAuthUseCase userAuthUseCase;
    private final UserCrudUseCase userCrudUseCase;
    private final Duration expiration;
    private final boolean secureCookie;

    public AuthController(UserAuthUseCase userAuthUseCase,
                          UserCrudUseCase userCrudUseCase,
                          @Value("${security.jwt.token.expirationHours}") long expirationHours,
                          @Value("${security.jwt.cookie-secure}") boolean secureCookie) {
        this.userAuthUseCase = userAuthUseCase;
        this.userCrudUseCase = userCrudUseCase;
        this.expiration = Duration.ofHours(expirationHours);
        this.secureCookie = secureCookie;
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponseEntity<?>> signIn(@Valid @RequestBody SignInRequestDTO requestDTO) {
        var token = userAuthUseCase.signIn(requestDTO.email(), requestDTO.password());

        var response = new ApiResponseEntity<>(
                true,
                "SignIn success",
                null,
                List.of());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenCookie(token, expiration).toString())
                .body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseEntity<?>> signUp(@Valid @RequestBody SignUpRequestDTO requestDTO) {
        userAuthUseCase.signUp(
                requestDTO.username(),
                requestDTO.email(),
                requestDTO.password());

        var response = new ApiResponseEntity<>(
                true,
                "SignUp success",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponseEntity<?>> signOut() {
        var response = new ApiResponseEntity<>(
                true,
                "SignOut success",
                null,
                List.of()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenCookie("", Duration.ZERO).toString())
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseEntity<?>> me(@AuthenticationPrincipal Jwt jwt) {
        var user = userCrudUseCase.getUserById(UUID.fromString(jwt.getSubject()));

        var response = new ApiResponseEntity<>(
                true,
                "Authenticated user",
                new UserResponseDTO(user),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    private ResponseCookie tokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from(SecurityConfig.TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
