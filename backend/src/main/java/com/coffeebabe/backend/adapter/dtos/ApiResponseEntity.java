package com.coffeebabe.backend.adapter.dtos;

import java.util.List;

public record ApiResponseEntity<T>(boolean success, String message, T data, List<?> errors) {
    public ApiResponseEntity(boolean success, String message, T data, List<?> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }
}
