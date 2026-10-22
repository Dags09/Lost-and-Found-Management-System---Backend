package com.lostandfound.backend.models;

import jakarta.persistence.*;

import java.time.Instant;

/** One issued refresh token. The id is the JWT "jti". A revoked row means that session is logged out. */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public RefreshToken() {}

    public RefreshToken(String id, String userId, String familyId, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFamilyId() { return familyId; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public Instant getCreatedAt() { return createdAt; }
}