package com.coffeebabe.backend.core.gateways;

import com.coffeebabe.backend.core.entities.Cart;
import com.coffeebabe.backend.core.entities.CartItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartCrudGateway {
    Optional<Cart> findItem(UUID userId, UUID productId);
    void saveItem(Cart item);
    void deleteItem(Cart item);
    List<CartItem> getItems(UUID userId);
}
