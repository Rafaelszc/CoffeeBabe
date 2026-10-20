package com.coffeebabe.backend.core.exceptions;

public class OrderStatusConflictException extends RuntimeException {
    public OrderStatusConflictException(String message) {
        super(message);
    }
}
