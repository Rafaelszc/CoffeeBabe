package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.core.entities.PageResult;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.entities.ProductImage;
import com.coffeebabe.backend.core.exceptions.ProductNotFoundException;
import com.coffeebabe.backend.core.usecases.ProductCrudUseCase;
import com.coffeebabe.backend.infra.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 7};

    @Autowired MockMvc mockMvc;
    @MockitoBean ProductCrudUseCase productCrudUseCase;

    @Test
    void searchReturnsPageWithTwoDecimalPricesAndImageUrl() throws Exception {
        var product = new Product("Cappuccino", "cremoso", new BigDecimal("4"), ProductCategory.HOT_COFFEE);
        when(productCrudUseCase.searchProducts("cap", ProductCategory.HOT_COFFEE, 0, 12))
                .thenReturn(new PageResult<>(List.of(product), 0, 12, 1, 1));

        mockMvc.perform(get("/api/product").param("q", "cap").param("category", "HOT_COFFEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.items[0].category").value("HOT_COFFEE"))
                .andExpect(jsonPath("$.data.items[0].imageUrl").value(org.hamcrest.Matchers.endsWith("/image")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"price\":4.00")));
    }

    @Test
    void unknownCategoryIs400() throws Exception {
        mockMvc.perform(get("/api/product").param("category", "TEA")).andExpect(status().isBadRequest());
    }

    @Test
    void imageIsServedWithServerContentTypeEtagAndNosniff() throws Exception {
        var id = UUID.randomUUID();
        when(productCrudUseCase.getImage(id)).thenReturn(new ProductImage(id, "image/png", PNG, "abc123"));

        mockMvc.perform(get("/api/product/" + id + "/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(PNG))
                .andExpect(header().string("ETag", "\"abc123\""))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));

        mockMvc.perform(get("/api/product/" + id + "/image").header("If-None-Match", "\"abc123\""))
                .andExpect(status().isNotModified());
    }

    @Test
    void missingImageIs404() throws Exception {
        var id = UUID.randomUUID();
        when(productCrudUseCase.getImage(id)).thenThrow(new ProductNotFoundException("x"));

        mockMvc.perform(get("/api/product/" + id + "/image")).andExpect(status().isNotFound());
    }
}
