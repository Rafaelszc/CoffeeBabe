package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.SignUpRequestDTO;
import com.coffeebabe.backend.adapter.dtos.UserResponseDTO;
import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.entities.UserRole;
import com.coffeebabe.backend.core.usecases.UserCrudUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserCrudUseCase userCrudUseCase;

    public UserController(UserCrudUseCase userCrudUseCase) {
        this.userCrudUseCase = userCrudUseCase;
    }

    @RequestMapping("/get")
    public ResponseEntity<ApiResponseEntity<?>> getUserById(@RequestParam UUID id) {
        var response = new ApiResponseEntity<>(
                true,
                "User from id: "+id,
                new UserResponseDTO(userCrudUseCase.getUserById(id)),
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseEntity<?>> saveUser(@RequestBody SignUpRequestDTO requestDTO) {
        userCrudUseCase.createUser(
                new User(
                requestDTO.username(),
                requestDTO.email(),
                requestDTO.password(),
                UserRole.ADMIN));

        var response = new ApiResponseEntity<>(
                true,
                requestDTO.username()+" created.",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
