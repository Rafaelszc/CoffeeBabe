package com.coffeebabe.backend.adapter.web.controller;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.adapter.dtos.SignInRequestDTO;
import com.coffeebabe.backend.adapter.dtos.SignInResponseDTO;
import com.coffeebabe.backend.adapter.dtos.SignUpRequestDTO;
import com.coffeebabe.backend.core.usecases.UserAuthUseCase;
import com.coffeebabe.backend.core.usecases.UserCrudUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAuthUseCase userAuthUseCase;

    public AuthController(UserAuthUseCase userAuthUseCase, UserCrudUseCase userCrudUseCase) {
        this.userAuthUseCase = userAuthUseCase;
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponseEntity<?>> signIn(@RequestBody SignInRequestDTO requestDTO) {
        var token = userAuthUseCase.signIn(requestDTO.email(), requestDTO.password());

        var response = new ApiResponseEntity<>(
                true,
                "SignIn success",
                new SignInResponseDTO(token),
                List.of());

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseEntity<?>> signUp(@RequestBody SignUpRequestDTO requestDTO) {
        userAuthUseCase.signUp(
                requestDTO.username(),
                requestDTO.email(),
                requestDTO.password());

        var response = new ApiResponseEntity<>(
                true,
                "SignUp success",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
