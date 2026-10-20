package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.core.entities.PageResult;
import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;
import com.coffeebabe.backend.core.usecases.ManageCartUseCase;
import com.coffeebabe.backend.core.usecases.OrderUseCase;
import com.coffeebabe.backend.core.usecases.ProductCrudUseCase;
import com.coffeebabe.backend.core.usecases.UserAuthUseCase;
import com.coffeebabe.backend.core.usecases.UserCrudUseCase;
import com.coffeebabe.backend.core.exceptions.UserNotAuthorizedException;
import com.coffeebabe.backend.infra.security.AttemptLimiter;
import com.coffeebabe.backend.infra.security.SecurityConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import({SecurityConfig.class, AttemptLimiter.class})
class SecurityRulesTest {
    private static final String PRODUCT_JSON = """
            {"name":"Latte","description":"d","price":5.00,"category":"HOT_COFFEE"}""";

    @Autowired MockMvc mockMvc;
    @Autowired JwtEncoder jwtEncoder;

    @MockitoBean UserAuthUseCase userAuthUseCase;
    @MockitoBean UserCrudUseCase userCrudUseCase;
    @MockitoBean ProductCrudUseCase productCrudUseCase;
    @MockitoBean ManageCartUseCase manageCartUseCase;
    @MockitoBean OrderUseCase orderUseCase;

    @Test
    void protectedRouteWithoutCookieReturns401() throws Exception {
        mockMvc.perform(get("/api/cart/get").param("userId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me").cookie(new Cookie(SecurityConfig.TOKEN_COOKIE, "garbage")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void productReadIsPublic() throws Exception {
        when(productCrudUseCase.searchProducts(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 12, 0, 0));

        mockMvc.perform(get("/api/product")).andExpect(status().isOk());
        mockMvc.perform(get("/api/product/get/best")).andExpect(status().isOk());
    }

    @Test
    void userCannotWriteProducts() throws Exception {
        mockMvc.perform(post("/api/product").cookie(tokenCookie(UUID.randomUUID(), UserRole.USER))
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCT_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanWriteProducts() throws Exception {
        mockMvc.perform(post("/api/product").cookie(tokenCookie(UUID.randomUUID(), UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCT_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void userCannotEditDeleteOrUploadProducts() throws Exception {
        var id = UUID.randomUUID();
        var user = tokenCookie(UUID.randomUUID(), UserRole.USER);

        mockMvc.perform(put("/api/product/" + id).cookie(user)
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCT_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/product/" + id).cookie(user)).andExpect(status().isForbidden());
        mockMvc.perform(multipart("/api/product/" + id + "/image")
                        .file(new MockMultipartFile("file", new byte[]{1})).cookie(user))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotReadOtherUsers() throws Exception {
        mockMvc.perform(get("/api/user/get").param("id", UUID.randomUUID().toString())
                        .cookie(tokenCookie(UUID.randomUUID(), UserRole.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicUserCreationIsGone() throws Exception {
        mockMvc.perform(post("/api/user/create").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"email\":\"x@x.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signInSetsHttpOnlyCookieAndNoTokenInBody() throws Exception {
        when(userAuthUseCase.signIn("ana@mail.com", "senha-forte")).thenReturn("the-token");

        mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@mail.com\",\"password\":\"senha-forte\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", allOf(
                        startsWith(SecurityConfig.TOKEN_COOKIE + "=the-token"),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax"),
                        containsString("Path=/"),
                        not(containsString("Secure")))))
                .andExpect(content().string(not(containsString("the-token"))));
    }

    @Test
    void repeatedFailedSignInsForAnEmailAre429() throws Exception {
        when(userAuthUseCase.signIn("victim@mail.com", "wrong-pass")).thenThrow(new UserNotAuthorizedException("x"));
        var body = "{\"email\":\"Victim@mail.com\",\"password\":\"wrong-pass\"}";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content(body.replace("Victim", "victim")))
                    .andExpect(status().isUnauthorized());
        }
        // case differences in the email do not open a new window
        mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void successfulSignInResetsTheEmailCount() throws Exception {
        when(userAuthUseCase.signIn("bia@mail.com", "wrong-pass")).thenThrow(new UserNotAuthorizedException("x"));
        when(userAuthUseCase.signIn("bia@mail.com", "senha-forte")).thenReturn("t");
        var wrong = "{\"email\":\"bia@mail.com\",\"password\":\"wrong-pass\"}";
        var right = "{\"email\":\"bia@mail.com\",\"password\":\"senha-forte\"}";

        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < 4; i++) {
                mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content(wrong))
                        .andExpect(status().isUnauthorized());
            }
            mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content(right))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void securityHeadersAreSent() throws Exception {
        mockMvc.perform(get("/api/product/get/best"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void signOutClearsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/signout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }

    @Test
    void meReturnsUserWithoutPassword() throws Exception {
        var id = UUID.randomUUID();
        when(userCrudUseCase.getUserById(id)).thenReturn(new User("ana", "ana@mail.com", "the-hash", UserRole.USER));

        mockMvc.perform(get("/api/auth/me").cookie(tokenCookie(id, UserRole.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("ana@mail.com"))
                .andExpect(content().string(not(containsString("the-hash"))));
    }

    @Test
    void invalidSignUpReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"email\":\"not-an-email\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    void crossOriginWriteIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/signup").header("Origin", "http://evil.example")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana\",\"email\":\"ana@mail.com\",\"password\":\"senha-forte\"}"))
                .andExpect(status().isForbidden());
    }

    private Cookie tokenCookie(UUID userId, UserRole role) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .build();
        var token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims));
        return new Cookie(SecurityConfig.TOKEN_COOKIE, token.getTokenValue());
    }
}
