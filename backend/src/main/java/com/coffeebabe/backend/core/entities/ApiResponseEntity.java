package com.coffeebabe.backend.core.entities;

public class ApiResponseEntity<T> {
    private boolean success;
    private String message;
    private T data;
    private Object errors;
}
