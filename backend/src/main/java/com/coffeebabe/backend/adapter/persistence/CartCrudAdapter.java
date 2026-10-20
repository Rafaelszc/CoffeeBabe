package com.coffeebabe.backend.adapter.persistence;

import com.coffeebabe.backend.core.entities.Cart;
import com.coffeebabe.backend.core.entities.CartItem;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.gateways.CartCrudGateway;
import com.coffeebabe.backend.infra.persistence.jpa.CartRepository;
import com.coffeebabe.backend.infra.persistence.jpa.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CartCrudAdapter implements CartCrudGateway {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartCrudAdapter(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Cart> findItem(UUID userId, UUID productId) {
        return cartRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public void saveItem(Cart item) {
        cartRepository.save(item);
    }

    @Override
    public void deleteItem(Cart item) {
        cartRepository.delete(item);
    }

    @Override
    public List<CartItem> getItems(UUID userId) {
        var rows = cartRepository.findByUserId(userId);
        var products = productRepository.findAllById(rows.stream().map(Cart::getProductId).toList()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // rows whose product was deleted are already gone (ON DELETE CASCADE)
        return rows.stream()
                .filter(row -> products.containsKey(row.getProductId()))
                .map(row -> new CartItem(products.get(row.getProductId()), row.getQuantity()))
                .sorted(Comparator.comparing(item -> item.product().getName()))
                .toList();
    }
}
