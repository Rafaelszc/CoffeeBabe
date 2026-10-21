package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.SignInRequestDTO;
import com.coffeebabe.backend.adapter.dtos.SignUpRequestDTO;
import com.coffeebabe.backend.adapter.dtos.UserResponseDTO;
import com.coffeebabe.backend.core.exceptions.TooManyAttemptsException;
import com.coffeebabe.backend.core.usecases.UserAuthUseCase;
import com.coffeebabe.backend.core.usecases.UserCrudUseCase;
import com.coffeebabe.backend.infra.security.AttemptLimiter;
import com.coffeebabe.backend.infra.security.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAuthUseCase userAuthUseCase;
    private final UserCrudUseCase userCrudUseCase;
    private final AttemptLimiter attemptLimiter;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final boolean secureCookie;
    private final int maxAttemptsPerIp;
    private final int maxFailuresPerEmail;
    private final Duration attemptWindow;

    public AuthController(UserAuthUseCase userAuthUseCase,
                          UserCrudUseCase userCrudUseCase,
                          AttemptLimiter attemptLimiter,
                          @Value("${security.jwt.token.expirationMinutes}") long accessTtlMinutes,
                          @Value("${security.refresh-token.ttl-days}") long refreshTtlDays,
                          @Value("${security.jwt.cookie-secure}") boolean secureCookie,
                          @Value("${app.auth.max-attempts-per-ip}") int maxAttemptsPerIp,
                          @Value("${app.auth.max-failures-per-email}") int maxFailuresPerEmail,
                          @Value("${app.auth.attempt-window-minutes}") long attemptWindowMinutes) {
        this.userAuthUseCase = userAuthUseCase;
        this.userCrudUseCase = userCrudUseCase;
        this.attemptLimiter = attemptLimiter;
        this.accessTtl = Duration.ofMinutes(accessTtlMinutes);
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
        this.secureCookie = secureCookie;
        this.maxAttemptsPerIp = maxAttemptsPerIp;
        this.maxFailuresPerEmail = maxFailuresPerEmail;
        this.attemptWindow = Duration.ofMinutes(attemptWindowMinutes);
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponseEntity<?>> signIn(@Valid @RequestBody SignInRequestDTO requestDTO,
                                                       HttpServletRequest request) {
        var emailKey = "signin-email:" + requestDTO.email().trim().toLowerCase(Locale.ROOT);
        limit(request);
        if (!attemptLimiter.tryAcquire(emailKey, maxFailuresPerEmail, attemptWindow)) {
            throw new TooManyAttemptsException("TooManyAttemptsException");
        }

        var tokens = userAuthUseCase.signIn(requestDTO.email(), requestDTO.password());
        // only failures count against the email
        attemptLimiter.reset(emailKey);

        var response = new ApiResponseEntity<>(
                true,
                "SignIn success",
                null,
                List.of());

        return withSessionCookies(tokens.accessToken(), tokens.refreshToken(), false).body(response);
    }

    // The refresh cookie only travels to /api/auth (Path), and SameSite=Strict keeps it off cross-site requests
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseEntity<?>> refresh(
            @CookieValue(name = SecurityConfig.REFRESH_COOKIE, required = false) String refreshToken) {
        var tokens = userAuthUseCase.refresh(refreshToken);

        var response = new ApiResponseEntity<>(
                true,
                "Session refreshed",
                null,
                List.of());

        return withSessionCookies(tokens.accessToken(), tokens.refreshToken(), false).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseEntity<?>> signUp(@Valid @RequestBody SignUpRequestDTO requestDTO,
                                                       HttpServletRequest request) {
        limit(request);
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
    public ResponseEntity<ApiResponseEntity<?>> signOut(
            @CookieValue(name = SecurityConfig.REFRESH_COOKIE, required = false) String refreshToken) {
        userAuthUseCase.signOut(refreshToken);

        var response = new ApiResponseEntity<>(
                true,
                "SignOut success",
                null,
                List.of()
        );

        return withSessionCookies("", "", true).body(response);
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

    // remote address only: X-Forwarded-For is client-controlled and there is no trusted proxy
    private void limit(HttpServletRequest request) {
        if (!attemptLimiter.tryAcquire("auth-ip:" + request.getRemoteAddr(), maxAttemptsPerIp, attemptWindow)) {
            throw new TooManyAttemptsException("TooManyAttemptsException");
        }
    }

    private ResponseEntity.BodyBuilder withSessionCookies(String accessToken, String refreshToken, boolean clear) {
        var access = ResponseCookie.from(SecurityConfig.TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(clear ? Duration.ZERO : accessTtl)
                .build();
        var refresh = ResponseCookie.from(SecurityConfig.REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(clear ? Duration.ZERO : refreshTtl)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, access.toString(), refresh.toString());
    }
}
