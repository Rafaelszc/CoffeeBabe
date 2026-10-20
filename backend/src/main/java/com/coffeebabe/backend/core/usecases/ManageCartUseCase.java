package com.coffeebabe.backend.core.usecases;

import com.coffeebabe.backend.core.entities.CartItem;
import com.coffeebabe.backend.core.entities.PriceSummary;

import java.util.List;
import java.util.UUID;

public interface ManageCartUseCase {
    record CartSummary(List<CartItem> items, PriceSummary prices) {}

    void addToCart(UUID userId, UUID productId);
    void removeFromCart(UUID userId, UUID productId);
    void decreaseItemQuantity(UUID userId, UUID productId);
    CartSummary getCart(UUID userId);
}
