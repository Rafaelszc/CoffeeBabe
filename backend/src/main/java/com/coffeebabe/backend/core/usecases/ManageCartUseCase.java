package com.coffeebabe.backend.core.usecases;

import com.coffeebabe.backend.core.entities.Product;

import java.util.List;
import java.util.UUID;

public interface ManageCartUseCase {
    void addToCard(UUID productId, UUID userId);
    void removeFromCart(UUID productId, UUID userId);
    List<Product> getCartProducts(UUID userId);
}
