package com.lostandfound.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;


/**
 * Per-IP rate limit on the public auth endpoints (login, register, ...) to slow down password
 * guessing and email flooding. Behind a proxy set server.forward-headers-strategy=native so
 * the real client IP is used.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final long MINUTE = 60_000;
    private static final long QUARTER_HOUR = 15 * MINUTE;

    private record Rule(int max, long windowMs) {}

    private static final Map<String, Rule> RULES = Map.of(
            "/v1/auth/login",               new Rule(20, MINUTE),
            "/v1/auth/register",            new Rule(10, MINUTE),
            "/v1/auth/refresh",             new Rule(30, MINUTE),
            "/v1/auth/forgot-password",     new Rule(5, QUARTER_HOUR),
            "/v1/auth/resend-verification", new Rule(5, QUARTER_HOUR),
            "/v1/auth/reset-password",      new Rule(10, QUARTER_HOUR),
            "/v1/auth/verify-email",        new Rule(20, QUARTER_HOUR));

    private final RateLimiter limiter;

    public AuthRateLimitFilter(RateLimiter limiter) {
        this.limiter = limiter;
    }

    /** Only POSTs to the listed auth URLs are limited; everything else passes straight through. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return !"POST".equals(req.getMethod()) || !RULES.containsKey(req.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        Rule rule = RULES.get(req.getRequestURI());
        // Counted per client IP and per URL. 0 = allowed, otherwise seconds until the window resets.
        long retryAfter = limiter.tryAcquire("auth:" + req.getRemoteAddr() + ":" + req.getRequestURI(),
                rule.max(), rule.windowMs());
        if (retryAfter > 0) {
            res.setStatus(429);
            res.setHeader("Retry-After", String.valueOf(retryAfter));
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Too many attempts, try again later\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}