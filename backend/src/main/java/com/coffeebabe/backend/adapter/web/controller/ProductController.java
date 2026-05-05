package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.ProductRequestDTO;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.usecases.ProductCrudUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductCrudUseCase productCrudUseCase;

    public ProductController(ProductCrudUseCase productCrudUseCase) {
        this.productCrudUseCase = productCrudUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseEntity<?>> createProduct(@RequestBody ProductRequestDTO requestDTO) {
        productCrudUseCase.publishProduct(new Product(
                requestDTO.name(),
                requestDTO.imageUrl(),
                requestDTO.description(),
                requestDTO.price()
        ));

        var response = new ApiResponseEntity<>(
                true,
                requestDTO.name()+" published successfully",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get/all")
    public ResponseEntity<ApiResponseEntity<?>> getAllProducts() {
        var products = productCrudUseCase.getAllProducts();

        var response = new ApiResponseEntity<>(
                true,
                "Get all products successffuly",
                products,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponseEntity<?>> findProductById(@RequestParam UUID id) {
        var response = new ApiResponseEntity<>(
                true,
                "Product find successffuly",
                productCrudUseCase.getProductById(id),
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }
}
