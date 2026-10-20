package com.coffeebabe.backend.core.gateways;

import com.coffeebabe.backend.core.entities.PageResult;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.entities.ProductImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCrudGateway {
    void saveProduct(Product product);
    Product getProductById(UUID productId);
    Optional<Product> findProductByName(String name);
    PageResult<Product> searchProducts(String query, ProductCategory category, int page, int size);
    void deleteProductById(UUID productId);
    List<Product> getBestSellers();
    void saveImage(ProductImage image);
    Optional<ProductImage> findImage(UUID productId);
}
