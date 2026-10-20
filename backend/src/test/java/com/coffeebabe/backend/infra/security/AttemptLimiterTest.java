package com.coffeebabe.backend.infra.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class AttemptLimiterTest {
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private Instant now = Instant.parse("2026-10-20T12:00:00Z");
    private final AttemptLimiter limiter = new AttemptLimiter(new Clock() {
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    });

    @Test
    void allowsUpToMaxInsideTheWindow() {
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryAcquire("k", 3, WINDOW)).isTrue();
        }
        assertThat(limiter.tryAcquire("k", 3, WINDOW)).isFalse();
        assertThat(limiter.tryAcquire("other", 3, WINDOW)).isTrue();
    }

    @Test
    void windowExpires() {
        for (int i = 0; i < 4; i++) limiter.tryAcquire("k", 3, WINDOW);

        now = now.plus(WINDOW).plusSeconds(1);

        assertThat(limiter.tryAcquire("k", 3, WINDOW)).isTrue();
    }

    @Test
    void resetClearsTheCount() {
        for (int i = 0; i < 4; i++) limiter.tryAcquire("k", 3, WINDOW);

        limiter.reset("k");

        assertThat(limiter.tryAcquire("k", 3, WINDOW)).isTrue();
    }
}
