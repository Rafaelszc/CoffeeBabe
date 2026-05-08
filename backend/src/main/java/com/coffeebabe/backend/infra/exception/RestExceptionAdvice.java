package com.coffeebabe.backend.infra.exception;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.core.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
public class RestExceptionAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponseEntity<?>> productNotFoundException(ProductNotFoundException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Product not found!",
                null,
                List.of(exception.getClass())
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponseEntity<?>> userAlreadyExistsException(UserAlreadyExistsException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "User already exists!",
                null,
                List.of(exception.getClass())
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseEntity<?>> userNotFoundException(UserNotFoundException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "User not found!",
                null,
                List.of(exception.getClass())
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ApiResponseEntity<?>> userNotAuthorizedException(UserNotAuthorizedException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "User not found!",
                null,
                List.of(exception.getClass())
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiResponseEntity<?>> invalidDataException(InvalidDataException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Invalid data!",
                null,
                List.of(exception.getClass())
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<ApiResponseEntity<?>> UnexpectedTypeException(UnexpectedTypeException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Invalid data!",
                null,
                List.of(exception.getClass())
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
