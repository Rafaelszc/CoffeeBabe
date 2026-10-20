package com.coffeebabe.backend.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Fixed-window attempt counter for sign-in and sign-up (SEC-5).
// shortcut: in-memory and per instance, fine for one backend; move to the database or Redis if it ever scales out
@Component
public class AttemptLimiter {
    private static final int CLEANUP_THRESHOLD = 10_000;

    private record Window(Instant start, int count) {}

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    @Autowired
    public AttemptLimiter() {
        this(Clock.systemUTC());
    }

    AttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    // Counts one attempt for key; false once more than max attempts happened inside the window.
    public boolean tryAcquire(String key, int max, Duration window) {
        var now = clock.instant();
        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.values().removeIf(w -> w.start().plus(window).isBefore(now));
        }

        var updated = windows.compute(key, (k, current) ->
                current == null || current.start().plus(window).isBefore(now)
                        ? new Window(now, 1)
                        : new Window(current.start(), current.count() + 1));
        return updated.count() <= max;
    }

    public void reset(String key) {
        windows.remove(key);
    }
}
