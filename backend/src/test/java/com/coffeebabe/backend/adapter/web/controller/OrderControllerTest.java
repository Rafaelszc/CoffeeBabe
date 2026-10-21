package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.core.entities.*;
import com.coffeebabe.backend.core.usecases.OrderUseCase;
import com.coffeebabe.backend.infra.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {
    private final UUID userId = UUID.randomUUID();
    private final List<CartItem> items = List.of(
            new CartItem(new Product("Mocha", "d", new BigDecimal("4.50"), ProductCategory.HOT_COFFEE), 1));
    private final Order order = new Order(userId, FulfillmentType.DELIVERY, "Rua A", items,
            PriceSummary.of(items, PriceSummary.DELIVERY_FEE));

    @Autowired MockMvc mockMvc;
    @MockitoBean OrderUseCase orderUseCase;

    @Test
    void placingIgnoresClientPricesAndReturnsServerTotals() throws Exception {
        when(orderUseCase.placeOrder(userId, FulfillmentType.DELIVERY, "Rua A")).thenReturn(order);

        mockMvc.perform(post("/api/orders").with(as("USER")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fulfillment\":\"DELIVERY\",\"address\":\"Rua A\",\"total\":0.01}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].productName").value("Mocha"))
                .andExpect(content().string(containsString("\"total\":24.64")));
    }

    @Test
    void missingFulfillmentIs400() throws Exception {
        mockMvc.perform(post("/api/orders").with(as("USER")).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelPassesRequesterAndRole() throws Exception {
        var id = UUID.randomUUID();
        when(orderUseCase.cancelOrder(userId, false, id)).thenReturn(order);

        mockMvc.perform(post("/api/orders/" + id + "/cancel").with(as("USER"))).andExpect(status().isOk());

        verify(orderUseCase).cancelOrder(userId, false, id);
    }

    @Test
    void onlyAdminListsAllAndChangesStatus() throws Exception {
        var id = UUID.randomUUID();
        when(orderUseCase.listAllOrders(anyInt(), anyInt())).thenReturn(new PageResult<>(List.of(), 0, 12, 0, 0));
        when(orderUseCase.updateStatus(any(), any())).thenReturn(order);
        var statusBody = "{\"status\":\"CONFIRMED\"}";

        mockMvc.perform(get("/api/orders/all").with(as("USER"))).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/orders/" + id + "/status").with(as("USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/orders/all").with(as("ADMIN"))).andExpect(status().isOk());
        mockMvc.perform(patch("/api/orders/" + id + "/status").with(as("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody))
                .andExpect(status().isOk());
        verify(orderUseCase).updateStatus(id, OrderStatus.CONFIRMED);
    }

    @Test
    void ordersRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
    }

    private RequestPostProcessor as(String role) {
        return jwt().jwt(j -> j.subject(userId.toString()).claim("role", role))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
