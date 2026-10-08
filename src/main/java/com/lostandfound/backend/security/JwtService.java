package com.lostandfound.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessExpiresMs;
    private final long refreshExpiresMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.refresh-secret}") String refreshSecret,
                      @Value("${jwt.expires-in}") long accessExpiresMs,
                      @Value("${jwt.refresh-expires-in}") long refreshExpiresMs) {
        this.accessKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiresMs = accessExpiresMs;
        this.refreshExpiresMs = refreshExpiresMs;
    }

    public String generateAccessToken(String userId, String role) {
        return build(userId, role, accessKey, accessExpiresMs);
    }

    public String generateRefreshToken(String userId, String role) {
        return build(userId, role, refreshKey, refreshExpiresMs);
    }

    private String build(String userId, String role, SecretKey key, long ttlMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMs))
                .signWith(key)
                .compact();
    }

    public Claims parseAccessToken(String token) throws JwtException {
        return parse(token, accessKey);
    }

    public Claims parseRefreshToken(String token) throws JwtException {
        return parse(token, refreshKey);
    }

    private Claims parse(String token, SecretKey key) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}