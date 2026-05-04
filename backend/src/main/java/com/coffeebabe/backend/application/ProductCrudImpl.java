package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import com.coffeebabe.backend.core.usecases.ProductCrudUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductCrudImpl implements ProductCrudUseCase {
    private final ProductCrudGateway productCrudGateway;

    public ProductCrudImpl(ProductCrudGateway productCrudGateway) {
        this.productCrudGateway = productCrudGateway;
    }

    @Override
    public List<Product> getAllProducts() {
        return productCrudGateway.getAllProducts();
    }

    @Override
    public Product getProductById(UUID id) {
        return productCrudGateway.getProductById(id);
    }

    @Override
    public void publishProduct(Product product) {
        productCrudGateway.saveProduct(product);
    }

    @Override
    public void removeProduct(UUID productId) {
        productCrudGateway.deleteProductById(productId);
    }
}
