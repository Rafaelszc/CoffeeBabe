package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.CartRequestDTO;
import com.coffeebabe.backend.core.usecases.ManageCartUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final ManageCartUseCase manageCartUseCase;

    public CartController(ManageCartUseCase manageCartUseCase) {
        this.manageCartUseCase = manageCartUseCase;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseEntity<?>> deleteFromCart(@RequestParam UUID productId, @RequestParam UUID userId) {
        manageCartUseCase.removeFromCart(productId, userId);

        var response = new ApiResponseEntity<>(
                true,
                productId+" removed from cart",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
    @PatchMapping("/decrease")
    public ResponseEntity<ApiResponseEntity<?>> decreaseFromCart(@RequestParam UUID productId, @RequestParam UUID userId) {
        manageCartUseCase.decreaseItemQuantity(productId, userId);

        var response = new ApiResponseEntity<>(
                true,
                productId+" decreased from cart",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponseEntity<?>> addToCart(@RequestBody CartRequestDTO requestDTO) {
        manageCartUseCase.addToCard(requestDTO.productId(), requestDTO.userId());

        var response = new ApiResponseEntity<>(
                true,
                requestDTO.productId()+" added to cart",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponseEntity<?>> getAllItems(@RequestParam UUID userId) {
        var response = new ApiResponseEntity<>(
                true,
                "All items returned successfuly",
                manageCartUseCase.getCartProducts(userId),
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }
}
