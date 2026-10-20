package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.core.entities.CartItem;
import com.coffeebabe.backend.core.entities.PriceSummary;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.usecases.ManageCartUseCase;
import com.coffeebabe.backend.core.usecases.ManageCartUseCase.CartSummary;
import com.coffeebabe.backend.infra.security.SecurityConfig;
import com.coffeebabe.backend.infra.web.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import({SecurityConfig.class, WebConfig.class})
class CartControllerTest {
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @Autowired MockMvc mockMvc;
    @MockitoBean ManageCartUseCase manageCartUseCase;

    @Test
    void operationsUseTheTokenUserAndIgnoreUserIdParams() throws Exception {
        var other = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/cart/add").with(jwt().jwt(j -> j.subject(userId.toString())))
                        .param("userId", other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\",\"userId\":\"" + other + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/cart/decrease").with(jwt().jwt(j -> j.subject(userId.toString())))
                        .param("productId", productId.toString()).param("userId", other))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/cart/delete").with(jwt().jwt(j -> j.subject(userId.toString())))
                        .param("productId", productId.toString()).param("userId", other))
                .andExpect(status().isOk());

        verify(manageCartUseCase).addToCart(userId, productId);
        verify(manageCartUseCase).decreaseItemQuantity(userId, productId);
        verify(manageCartUseCase).removeFromCart(userId, productId);
    }

    @Test
    void cartListsItemsWithQuantityAndTotals() throws Exception {
        var latte = new Product("Latte", "d", new BigDecimal("4.00"), ProductCategory.HOT_COFFEE);
        var items = List.of(new CartItem(latte, 2));
        when(manageCartUseCase.getCart(userId)).thenReturn(new CartSummary(items, PriceSummary.of(items, BigDecimal.ZERO)));

        mockMvc.perform(get("/api/cart/get").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.products[0].quantity").value(2))
                .andExpect(jsonPath("$.data.products[0].product.name").value("Latte"))
                .andExpect(content().string(containsString("\"lineTotal\":8.00")))
                .andExpect(content().string(containsString("\"subtotal\":8.00")))
                .andExpect(content().string(containsString("\"tax\":0.24")))
                .andExpect(content().string(containsString("\"total\":8.24")));
    }

    @Test
    void missingProductIdIs400() throws Exception {
        mockMvc.perform(post("/api/cart/add").with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void browserPreflightAllowsPatchFromFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/cart/decrease")
                        .header("Origin", "http://localhost:5174")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("PATCH")))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
