package com.coffeebabe.backend.adapter.persistence;

import com.coffeebabe.backend.core.entities.Cart;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.gateways.CartCrudGateway;
import com.coffeebabe.backend.infra.persistence.jpa.CartRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CartCrudAdapter implements CartCrudGateway {
    private final CartRepository cartRepository;

    public CartCrudAdapter(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public void saveItemToCart(UUID productId, UUID userId) {
        cartRepository.save(new Cart(userId, productId));
    }

    @Override
    public List<Product> getItemsFromCart(UUID userId) {
        return cartRepository.findAllProductsFromUserId(userId);
    }

    @Override
    public void removeItemFromCart(UUID productId, UUID userId) {
        cartRepository.deleteByProductIdAndUserId(productId, userId);
    }

    @Override
    public void removeFirstFromCart(UUID productId, UUID userId) {
        cartRepository.deleteFirstByProductIdAndUserId(productId, userId);
    }
}
