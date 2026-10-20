package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.Cart;
import com.coffeebabe.backend.core.entities.PriceSummary;
import com.coffeebabe.backend.core.gateways.CartCrudGateway;
import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import com.coffeebabe.backend.core.usecases.ManageCartUseCase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ManageCartImpl implements ManageCartUseCase {
    private final CartCrudGateway cartCrudGateway;
    private final ProductCrudGateway productCrudGateway;

    public ManageCartImpl(CartCrudGateway cartCrudGateway, ProductCrudGateway productCrudGateway) {
        this.cartCrudGateway = cartCrudGateway;
        this.productCrudGateway = productCrudGateway;
    }

    @Override
    public void addToCart(UUID userId, UUID productId) {
        // throws ProductNotFoundException (404) for unknown products
        productCrudGateway.getProductById(productId);

        var item = cartCrudGateway.findItem(userId, productId).orElse(null);
        if (item == null) {
            item = new Cart(userId, productId);
        } else {
            item.increase();
        }
        cartCrudGateway.saveItem(item);
    }

    @Override
    public void removeFromCart(UUID userId, UUID productId) {
        cartCrudGateway.findItem(userId, productId).ifPresent(cartCrudGateway::deleteItem);
    }

    @Override
    public void decreaseItemQuantity(UUID userId, UUID productId) {
        cartCrudGateway.findItem(userId, productId).ifPresent(item -> {
            if (item.getQuantity() > 1) {
                item.decrease();
                cartCrudGateway.saveItem(item);
            } else {
                cartCrudGateway.deleteItem(item);
            }
        });
    }

    @Override
    public CartSummary getCart(UUID userId) {
        var items = cartCrudGateway.getItems(userId);
        return new CartSummary(items, PriceSummary.of(items, BigDecimal.ZERO));
    }
}
