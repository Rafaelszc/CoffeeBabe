package com.coffeebabe.backend.core.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// Pricing rules for cart and order (PRD 3.4): tax is 3% of the products only, never of the delivery fee
public record PriceSummary(BigDecimal subtotal, BigDecimal tax, BigDecimal deliveryFee, BigDecimal total) {
    public static final BigDecimal TAX_RATE = new BigDecimal("0.03");
    public static final BigDecimal DELIVERY_FEE = new BigDecimal("20.00");

    public static PriceSummary of(List<CartItem> items, BigDecimal deliveryFee) {
        var subtotal = money(items.stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        var tax = money(subtotal.multiply(TAX_RATE));
        var fee = money(deliveryFee);
        return new PriceSummary(subtotal, tax, fee, subtotal.add(tax).add(fee));
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
