package com.coffeebabe.backend.core.usecases;

import com.coffeebabe.backend.core.entities.PageResult;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.entities.ProductImage;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductCrudUseCase {
    PageResult<Product> searchProducts(String query, ProductCategory category, int page, int size);
    List<Product> getBestSellers();
    Product getProductById(UUID id);
    void publishProduct(Product product);
    void updateProduct(UUID id, String name, String description, BigDecimal price, ProductCategory category);
    void removeProduct(UUID productId);
    void uploadImage(UUID productId, byte[] data);
    ProductImage getImage(UUID productId);
}
