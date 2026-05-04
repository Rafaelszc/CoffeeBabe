package com.coffeebabe.backend.core.usecases;

import com.coffeebabe.backend.core.entities.Product;

import java.util.List;
import java.util.UUID;

public interface ProductCrudUseCase {
    List<Product> getAllProducts();
    Product getProductById(UUID id);
    void publishProduct(Product product);
    void removeProduct(UUID productId);
}
