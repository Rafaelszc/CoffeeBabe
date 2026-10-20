package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.OrderResponseDTO;
import com.coffeebabe.backend.adapter.dtos.OrderStatusRequestDTO;
import com.coffeebabe.backend.adapter.dtos.PlaceOrderRequestDTO;
import com.coffeebabe.backend.core.entities.UserRole;
import com.coffeebabe.backend.core.usecases.OrderUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// /all and PATCH /{id}/status are ADMIN only (SecurityConfig); the rest acts on the token's user
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponseEntity<?>> placeOrder(@AuthenticationPrincipal Jwt jwt,
                                                           @Valid @RequestBody PlaceOrderRequestDTO requestDTO) {
        var order = orderUseCase.placeOrder(userId(jwt), requestDTO.fulfillment(), requestDTO.address());

        var response = new ApiResponseEntity<>(
                true,
                "Order placed successfully",
                new OrderResponseDTO(order),
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponseEntity<?>> listOwnOrders(@AuthenticationPrincipal Jwt jwt,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "12") int size) {
        var response = new ApiResponseEntity<>(
                true,
                "Orders found successfully",
                orderUseCase.listOwnOrders(userId(jwt), page, size).map(OrderResponseDTO::new),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponseEntity<?>> listAllOrders(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "12") int size) {
        var response = new ApiResponseEntity<>(
                true,
                "Orders found successfully",
                orderUseCase.listAllOrders(page, size).map(OrderResponseDTO::new),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseEntity<?>> getOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        var response = new ApiResponseEntity<>(
                true,
                "Order found successfully",
                new OrderResponseDTO(orderUseCase.getOrder(userId(jwt), isAdmin(jwt), id)),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponseEntity<?>> cancelOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        var response = new ApiResponseEntity<>(
                true,
                "Order cancelled successfully",
                new OrderResponseDTO(orderUseCase.cancelOrder(userId(jwt), isAdmin(jwt), id)),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseEntity<?>> updateStatus(@PathVariable UUID id,
                                                             @Valid @RequestBody OrderStatusRequestDTO requestDTO) {
        var response = new ApiResponseEntity<>(
                true,
                "Order status updated successfully",
                new OrderResponseDTO(orderUseCase.updateStatus(id, requestDTO.status())),
                List.of()
        );

        return ResponseEntity.ok(response);
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static boolean isAdmin(Jwt jwt) {
        return UserRole.ADMIN.name().equals(jwt.getClaimAsString("role"));
    }
}
