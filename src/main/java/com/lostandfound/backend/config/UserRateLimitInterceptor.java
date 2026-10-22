package com.lostandfound.backend.config;

import com.lostandfound.backend.security.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.regex.Pattern;


/**
 * Per-user rate limits for the POST endpoints that create things (claims, reports, uploads), so a
 * single account cannot flood the system. The public auth endpoints are limited per IP by
 * AuthRateLimitFilter instead.
 */
@Component
public class UserRateLimitInterceptor implements HandlerInterceptor {

    private static final long HOUR = 3_600_000;

    private record Rule(String name, Pattern path, int max, long windowMs, String message) {}

    private static final List<Rule> RULES = List.of(
            new Rule("claim",     Pattern.compile("^/v1/items/[^/]+/claims$"), 5, HOUR,
                    "You are submitting claims too quickly, try again later"),
            new Rule("item",      Pattern.compile("^/v1/items$"), 10, HOUR,
                    "You are reporting items too quickly, try again later"),
            new Rule("lost-item", Pattern.compile("^/v1/lost-items$"), 10, HOUR,
                    "You are creating reports too quickly, try again later"),
            new Rule("upload",    Pattern.compile("^/v1/(items|lost-items)/[^/]+/images$"), 40, HOUR,
                    "You are uploading images too quickly, try again later"));

    private final RateLimiter limiter;

    public UserRateLimitInterceptor(RateLimiter limiter) {
        this.limiter = limiter;
    }

    /**
     * Runs before the controller. Only POSTs by a logged-in user that match a rule are counted; when
     * the limit is hit the request ends with 429 and a Retry-After header.
     */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (!"POST".equals(req.getMethod())) return true;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return true;      // Spring Security already answers 401

        String path = req.getRequestURI();
        for (Rule r : RULES) {
            if (!r.path().matcher(path).matches()) continue;
            long retryAfter = limiter.tryAcquire("user:" + r.name() + ":" + auth.getPrincipal(), r.max(), r.windowMs());
            if (retryAfter > 0) {
                res.setStatus(429);
                res.setHeader("Retry-After", String.valueOf(retryAfter));
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"" + r.message() + "\"}");
                return false;
            }
            break;
        }
        return true;
    }
}