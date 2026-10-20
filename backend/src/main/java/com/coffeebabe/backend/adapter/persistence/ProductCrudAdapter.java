package com.coffeebabe.backend.adapter.persistence;

import com.coffeebabe.backend.core.entities.PageResult;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.entities.ProductImage;
import com.coffeebabe.backend.core.exceptions.ProductNotFoundException;
import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import com.coffeebabe.backend.infra.persistence.jpa.ProductImageRepository;
import com.coffeebabe.backend.infra.persistence.jpa.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProductCrudAdapter implements ProductCrudGateway {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductCrudAdapter(ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    @Override
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    @Override
    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("ProductNotFoundException"));
    }

    @Override
    public Optional<Product> findProductByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public PageResult<Product> searchProducts(String query, ProductCategory category, int page, int size) {
        Specification<Product> spec = (root, criteriaQuery, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (!query.isBlank()) {
                var pattern = "%" + query.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        var result = productRepository.findAll(spec, PageRequest.of(page, size, Sort.by("name")));
        return new PageResult<>(result.getContent(), page, size, result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public void deleteProductById(UUID productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public List<Product> getBestSellers() {
        return productRepository.findAllBestSellers();
    }

    @Override
    public void saveImage(ProductImage image) {
        productImageRepository.save(image);
    }

    @Override
    public Optional<ProductImage> findImage(UUID productId) {
        return productImageRepository.findById(productId);
    }
}
