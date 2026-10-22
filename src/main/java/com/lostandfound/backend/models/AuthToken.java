package com.lostandfound.backend.models;

import jakarta.persistence.*;

import java.time.Instant;

/** A one-time token sent by email (verify email / reset password). Only its SHA-256 hash is stored. */
@Entity
@Table(name = "auth_tokens")
public class AuthToken {

    public enum Type { VERIFY_EMAIL, RESET_PASSWORD }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public AuthToken() {}

    public AuthToken(String userId, Type type, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.type = type;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public Type getType() { return type; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
}