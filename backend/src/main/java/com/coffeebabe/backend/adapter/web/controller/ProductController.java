package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.ProductRequestDTO;
import com.coffeebabe.backend.adapter.dtos.ProductResponseDTO;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.usecases.ProductCrudUseCase;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductCrudUseCase productCrudUseCase;

    public ProductController(ProductCrudUseCase productCrudUseCase) {
        this.productCrudUseCase = productCrudUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponseEntity<?>> searchProducts(@RequestParam(required = false) String q,
                                                               @RequestParam(required = false) ProductCategory category,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "12") int size) {
        var response = new ApiResponseEntity<>(
                true,
                "Products found successfully",
                productCrudUseCase.searchProducts(q, category, page, size).map(ProductResponseDTO::new),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponseEntity<?>> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        productCrudUseCase.publishProduct(new Product(
                requestDTO.name(),
                requestDTO.description(),
                requestDTO.price(),
                requestDTO.category()
        ));

        var response = new ApiResponseEntity<>(
                true,
                requestDTO.name()+" published successfully",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseEntity<?>> updateProduct(@PathVariable UUID id,
                                                              @Valid @RequestBody ProductRequestDTO requestDTO) {
        productCrudUseCase.updateProduct(id,
                requestDTO.name(),
                requestDTO.description(),
                requestDTO.price(),
                requestDTO.category());

        var response = new ApiResponseEntity<>(
                true,
                requestDTO.name()+" updated successfully",
                null,
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseEntity<?>> removeProduct(@PathVariable UUID id) {
        productCrudUseCase.removeProduct(id);

        var response = new ApiResponseEntity<>(
                true,
                id+" removed successfully",
                null,
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseEntity<?>> uploadImage(@PathVariable UUID id,
                                                            @RequestParam MultipartFile file) throws IOException {
        productCrudUseCase.uploadImage(id, file.getBytes());

        var response = new ApiResponseEntity<>(
                true,
                "Image uploaded successfully",
                null,
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    // ETag lets Spring answer If-None-Match with 304; Spring Security adds X-Content-Type-Options: nosniff
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID id) {
        var image = productCrudUseCase.getImage(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .eTag(image.getEtag())
                .cacheControl(CacheControl.noCache())
                .body(image.getData());
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponseEntity<?>> findProductById(@RequestParam UUID id) {
        var response = new ApiResponseEntity<>(
                true,
                "Product find successfully",
                new ProductResponseDTO(productCrudUseCase.getProductById(id)),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get/best")
    public ResponseEntity<ApiResponseEntity<?>> getBestSellers() {
        var response = new ApiResponseEntity<>(
                true,
                "Best sellers find successfully",
                productCrudUseCase.getBestSellers().stream().map(ProductResponseDTO::new).toList(),
                List.of()
        );

        return ResponseEntity.ok(response);
    }
}
