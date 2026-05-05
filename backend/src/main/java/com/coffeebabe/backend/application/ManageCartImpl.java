package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.gateways.CartCrudGateway;
import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import com.coffeebabe.backend.core.usecases.ManageCartUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public void addToCard(UUID productId, UUID userId) {
        var product = productCrudGateway.getProductById(productId);
        if (product != null) cartCrudGateway.saveItemToCart(productId, userId);
    }

    @Override
    public void removeFromCart(UUID productId, UUID userId) {
        cartCrudGateway.removeItemFromCart(productId, userId);
    }

    @Override
    public void decreaseItemQuantity(UUID productId, UUID userId) {
        cartCrudGateway.removeFirstFromCart(productId, userId);
    }

    @Override
    public List<Product> getCartProducts(UUID userId) {
        return cartCrudGateway.getItemsFromCart(userId);
    }
}
