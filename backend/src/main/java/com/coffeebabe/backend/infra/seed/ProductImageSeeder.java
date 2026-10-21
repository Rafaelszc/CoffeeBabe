package com.coffeebabe.backend.infra.seed;

import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import com.coffeebabe.backend.core.usecases.ProductCrudUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

// Attaches images to the V2 seed products that have none yet; never overwrites an admin upload (RF-CAT-8)
@Component
public class ProductImageSeeder implements ApplicationRunner {
    private static final Map<String, String> SEED_IMAGES = Map.of(
            "Cappuccino", "product-cappuccino.jpg",
            "Mocha", "product-mocha.jpg",
            "Cake", "product-cake.jpg");

    private final ProductCrudUseCase productCrudUseCase;
    private final ProductCrudGateway productCrudGateway;

    public ProductImageSeeder(ProductCrudUseCase productCrudUseCase, ProductCrudGateway productCrudGateway) {
        this.productCrudUseCase = productCrudUseCase;
        this.productCrudGateway = productCrudGateway;
    }

    @Override
    public void run(ApplicationArguments args) {
        SEED_IMAGES.forEach((name, file) -> productCrudGateway.findProductByName(name)
                .filter(product -> productCrudGateway.findImage(product.getId()).isEmpty())
                .ifPresent(product -> productCrudUseCase.uploadImage(product.getId(), read("seed/images/" + file))));
    }

    private static byte[] read(String path) {
        try {
            return new ClassPathResource(path).getContentAsByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
