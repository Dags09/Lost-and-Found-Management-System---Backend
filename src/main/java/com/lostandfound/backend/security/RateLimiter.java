package com.lostandfound.backend.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-memory fixed-window rate limiter, shared by the auth filter, the per-user interceptor and
 * the auth service. Counts live in this process only: with several app instances each keeps its own
 * counts (use Redis or a gateway limit if you scale out).
 */
@Component
public class RateLimiter {

    private static final int MAX_KEYS = 20_000;

    private static final class Window { long end; int count; }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Counts one hit for the key.
     * @return 0 if the hit is allowed, otherwise the number of seconds until the window resets.
     */
    public long tryAcquire(String key, int max, long windowMs) {
        long now = System.currentTimeMillis();
        if (windows.size() > MAX_KEYS) windows.values().removeIf(w -> w.end < now);   // keep memory bounded

        Window w = windows.computeIfAbsent(key, k -> new Window());
        synchronized (w) {
            if (now >= w.end) { w.end = now + windowMs; w.count = 0; }
            if (++w.count > max) return Math.max(1, (w.end - now + 999) / 1000);
        }
        return 0;
    }
}