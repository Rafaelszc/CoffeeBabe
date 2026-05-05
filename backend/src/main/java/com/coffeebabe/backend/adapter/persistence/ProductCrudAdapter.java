package com.coffeebabe.backend.adapter.persistence;

import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import com.coffeebabe.backend.infra.persistence.jpa.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProductCrudAdapter implements ProductCrudGateway {
    private final ProductRepository productRepository;

    public ProductCrudAdapter(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    @Override
    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElse(null);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public void deleteProductById(UUID productId) {
        productRepository.deleteById(productId);
    }
}
