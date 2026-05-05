package com.coffeebabe.backend.core.gateways;

import com.coffeebabe.backend.core.entities.Product;

import java.util.List;
import java.util.UUID;

public interface CartCrudGateway {
    void saveItemToCart(UUID productId, UUID userId);
    List<Product> getItemsFromCart(UUID userId);
    void removeItemFromCart(UUID productId, UUID userId);
    void removeFirstFromCart(UUID productId, UUID userId);
}
