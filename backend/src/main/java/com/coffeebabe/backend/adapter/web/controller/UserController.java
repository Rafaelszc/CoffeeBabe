package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.UserResponseDTO;
import com.coffeebabe.backend.core.usecases.UserCrudUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// ADMIN only (SecurityConfig); users read themselves via GET /api/auth/me
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserCrudUseCase userCrudUseCase;

    public UserController(UserCrudUseCase userCrudUseCase) {
        this.userCrudUseCase = userCrudUseCase;
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponseEntity<?>> getUserById(@RequestParam UUID id) {
        var response = new ApiResponseEntity<>(
                true,
                "User from id: "+id,
                new UserResponseDTO(userCrudUseCase.getUserById(id)),
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
