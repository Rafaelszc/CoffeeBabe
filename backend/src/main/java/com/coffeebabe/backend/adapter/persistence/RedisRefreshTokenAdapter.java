package com.coffeebabe.backend.adapter.persistence;

import com.coffeebabe.backend.core.gateways.RefreshTokenGateway;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

// Keys (ADR-0008): refresh:active:{hash} -> userId, refresh:used:{hash} -> userId|epochSeconds,
// refresh:user:{userId} -> set of active hashes
@Component
public class RedisRefreshTokenAdapter implements RefreshTokenGateway {
    private static final String ACTIVE = "refresh:active:";
    private static final String USED = "refresh:used:";
    private static final String USER = "refresh:user:";

    private final StringRedisTemplate redis;

    public RedisRefreshTokenAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void store(String tokenHash, UUID userId, Duration ttl) {
        redis.opsForValue().set(ACTIVE + tokenHash, userId.toString(), ttl);
        redis.opsForSet().add(USER + userId, tokenHash);
        redis.expire(USER + userId, ttl);
    }

    @Override
    public Optional<UUID> consume(String tokenHash, Duration usedMarkerTtl) {
        // GETDEL: of two concurrent refreshes with the same token, only one gets the user
        var userId = redis.opsForValue().getAndDelete(ACTIVE + tokenHash);
        if (userId == null) {
            return Optional.empty();
        }

        redis.opsForValue().set(USED + tokenHash, userId + "|" + Instant.now().getEpochSecond(), usedMarkerTtl);
        redis.opsForSet().remove(USER + userId, tokenHash);
        return Optional.of(UUID.fromString(userId));
    }

    @Override
    public Optional<UsedToken> findUsed(String tokenHash) {
        return Optional.ofNullable(redis.opsForValue().get(USED + tokenHash))
                .map(value -> value.split("\\|"))
                .map(parts -> new UsedToken(UUID.fromString(parts[0]), Instant.ofEpochSecond(Long.parseLong(parts[1]))));
    }

    @Override
    public void revoke(String tokenHash) {
        var userId = redis.opsForValue().getAndDelete(ACTIVE + tokenHash);
        if (userId != null) {
            redis.opsForSet().remove(USER + userId, tokenHash);
        }
    }

    @Override
    public void revokeAll(UUID userId) {
        var hashes = redis.opsForSet().members(USER + userId);
        if (hashes != null && !hashes.isEmpty()) {
            redis.delete(hashes.stream().map(hash -> ACTIVE + hash).toList());
        }
        redis.delete(USER + userId);
    }
}
