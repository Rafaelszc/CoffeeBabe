package com.coffeebabe.backend.core.entities;

// accessToken: short-lived JWT; refreshToken: opaque, single-use (ADR-0008)
public record AuthTokens(String accessToken, String refreshToken) {
}
