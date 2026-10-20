package com.coffeebabe.backend.core.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriceSummaryTest {
    private static final List<CartItem> PRD_EXAMPLE = List.of(
            new CartItem(new Product("Cappuccino", "d", new BigDecimal("4.00"), ProductCategory.HOT_COFFEE), 2),
            new CartItem(new Product("Mocha", "d", new BigDecimal("4.50"), ProductCategory.HOT_COFFEE), 1));

    @Test
    void prdExampleWithDelivery() {
        var prices = PriceSummary.of(PRD_EXAMPLE, PriceSummary.DELIVERY_FEE);

        assertThat(prices.subtotal()).isEqualTo(new BigDecimal("12.50"));
        assertThat(prices.tax()).isEqualTo(new BigDecimal("0.38"));
        assertThat(prices.deliveryFee()).isEqualTo(new BigDecimal("20.00"));
        assertThat(prices.total()).isEqualTo(new BigDecimal("32.88"));
    }

    @Test
    void pickupHasNoFeeAndTaxIgnoresIt() {
        var prices = PriceSummary.of(PRD_EXAMPLE, BigDecimal.ZERO);

        assertThat(prices.deliveryFee()).isEqualTo(new BigDecimal("0.00"));
        assertThat(prices.total()).isEqualTo(new BigDecimal("12.88"));
    }

    @Test
    void emptyCartIsZero() {
        var prices = PriceSummary.of(List.of(), BigDecimal.ZERO);

        assertThat(prices.total()).isEqualTo(new BigDecimal("0.00"));
    }
}
