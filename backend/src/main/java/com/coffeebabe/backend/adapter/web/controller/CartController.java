package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.CartRequestDTO;
import com.coffeebabe.backend.adapter.dtos.CartResponseDTO;
import com.coffeebabe.backend.core.usecases.ManageCartUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// The cart always belongs to the token's user; userId is never read from the request (SEC-7)
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final ManageCartUseCase manageCartUseCase;

    public CartController(ManageCartUseCase manageCartUseCase) {
        this.manageCartUseCase = manageCartUseCase;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseEntity<?>> deleteFromCart(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID productId) {
        manageCartUseCase.removeFromCart(userId(jwt), productId);

        var response = new ApiResponseEntity<>(
                true,
                productId+" removed from cart",
                null,
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/decrease")
    public ResponseEntity<ApiResponseEntity<?>> decreaseFromCart(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID productId) {
        manageCartUseCase.decreaseItemQuantity(userId(jwt), productId);

        var response = new ApiResponseEntity<>(
                true,
                productId+" decreased from cart",
                null,
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponseEntity<?>> addToCart(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody CartRequestDTO requestDTO) {
        manageCartUseCase.addToCart(userId(jwt), requestDTO.productId());

        var response = new ApiResponseEntity<>(
                true,
                requestDTO.productId()+" added to cart",
                null,
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponseEntity<?>> getAllItems(@AuthenticationPrincipal Jwt jwt) {
        var response = new ApiResponseEntity<>(
                true,
                "All items returned successfully",
                new CartResponseDTO(manageCartUseCase.getCart(userId(jwt))),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
