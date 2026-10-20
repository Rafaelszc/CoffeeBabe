package com.coffeebabe.backend.core.gateways;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

// Stores only token hashes, never the tokens themselves (SEC-20)
public interface RefreshTokenGateway {
    record UsedToken(UUID userId, Instant usedAt) {}

    void store(String tokenHash, UUID userId, Duration ttl);

    // Atomically removes an active token and remembers it as used for usedMarkerTtl
    Optional<UUID> consume(String tokenHash, Duration usedMarkerTtl);

    Optional<UsedToken> findUsed(String tokenHash);

    void revoke(String tokenHash);

    void revokeAll(UUID userId);
}
