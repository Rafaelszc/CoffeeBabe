package com.coffeebabe.backend.infra.exception;

import com.coffeebabe.backend.adapter.dtos.ApiResponseEntity;
import com.coffeebabe.backend.core.exceptions.*;
import jakarta.validation.UnexpectedTypeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
public class RestExceptionAdvice extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        var response = new ApiResponseEntity<>(
                false,
                "Invalid data!",
                null,
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponseEntity<?>> productNotFoundException(ProductNotFoundException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Product not found!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        var response = new ApiResponseEntity<>(
                false,
                "Image too large! Maximum is 2 MB.",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.CONTENT_TOO_LARGE);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiResponseEntity<?>> productAlreadyExistsException(ProductAlreadyExistsException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Product already exists!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponseEntity<?>> orderNotFoundException(OrderNotFoundException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Order not found!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderStatusConflictException.class)
    public ResponseEntity<ApiResponseEntity<?>> orderStatusConflictException(OrderStatusConflictException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Invalid order status change!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponseEntity<?>> userAlreadyExistsException(UserAlreadyExistsException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "User already exists!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseEntity<?>> userNotFoundException(UserNotFoundException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "User not found!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ApiResponseEntity<?>> userNotAuthorizedException(UserNotAuthorizedException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Invalid email or password!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiResponseEntity<?>> invalidDataException(InvalidDataException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Invalid data!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<ApiResponseEntity<?>> UnexpectedTypeException(UnexpectedTypeException exception) {
        var response = new ApiResponseEntity<>(
                false,
                "Invalid data!",
                null,
                List.of()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
