package com.coffeebabe.backend.core.entities;

import java.math.BigDecimal;

public record CartItem(Product product, int quantity) {
    public BigDecimal lineTotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
