package com.coffeebabe.backend.core.entities;

import com.coffeebabe.backend.core.exceptions.InvalidDataException;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
    public static final int MAX_PAGE_SIZE = 50;

    // RNF-4 / SEC-11: every listing is paged, at most 50 per page
    public static void checkPaging(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidDataException("InvalidDataException");
        }
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(items.stream().map(mapper).toList(), page, size, totalItems, totalPages);
    }
}
