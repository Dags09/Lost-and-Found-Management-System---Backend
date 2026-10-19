package com.lostandfound.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000;
    private static final Set<String> LIMITED =
            Set.of("/v1/auth/login", "/v1/auth/register", "/v1/auth/refresh");

    private static final class Window { long start; int count; }

    private final ConcurrentHashMap<String, Window> hits = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return !"POST".equals(req.getMethod()) || !LIMITED.contains(req.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        if (hits.size() > 10_000)                       // keep memory bounded
            hits.values().removeIf(w -> now - w.start > WINDOW_MS);

        Window w = hits.computeIfAbsent(req.getRemoteAddr() + req.getRequestURI(), k -> new Window());
        boolean blocked;
        synchronized (w) {
            if (now - w.start > WINDOW_MS) { w.start = now; w.count = 0; }
            blocked = ++w.count > MAX_REQUESTS;
        }

        if (blocked) {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Too many attempts, try again in a minute\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}