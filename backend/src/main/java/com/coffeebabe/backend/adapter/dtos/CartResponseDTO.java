package com.coffeebabe.backend.adapter.dtos;

import com.coffeebabe.backend.core.entities.CartItem;
import com.coffeebabe.backend.core.usecases.ManageCartUseCase.CartSummary;

import java.math.BigDecimal;
import java.util.List;

// Field names match what the frontend Checkout reads (products, subtotal, tax)
public record CartResponseDTO(List<Item> products, BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    public record Item(ProductResponseDTO product, int quantity, BigDecimal lineTotal) {
        Item(CartItem item) {
            this(new ProductResponseDTO(item.product()), item.quantity(), item.lineTotal());
        }
    }

    public CartResponseDTO(CartSummary cart) {
        this(cart.items().stream().map(Item::new).toList(),
                cart.prices().subtotal(),
                cart.prices().tax(),
                cart.prices().total());
    }
}
