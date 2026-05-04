package com.coffeebabe.backend.core.gateways;

import com.coffeebabe.backend.core.entities.Product;

import java.util.List;
import java.util.UUID;

public interface ProductCrudGateway {
    void saveProduct(Product product);
    Product getProductById(UUID productId);
    List<Product> getAllProducts();
    void deleteProductById(UUID productId);
}
