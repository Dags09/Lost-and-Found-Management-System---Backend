package com.lostandfound.backend.service;

import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.RefreshToken;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.repository.RefreshTokenRepository;
import com.lostandfound.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Server-side record of every refresh token, so sessions can be ended.
 *
 * - Login starts a new "family". Each refresh revokes the used token and issues a new one in the same family.
 * - If an already-used token shows up again (it was probably stolen), the whole family is revoked.
 * - Logout revokes the family. Password reset and logout-all revoke every token of the user.
 */
@Service
public class RefreshTokenService {

    public record Rotation(String userId, String familyId) {}

    private final RefreshTokenRepository tokens;
    private final JwtService jwt;

    public RefreshTokenService(RefreshTokenRepository tokens, JwtService jwt) {
        this.tokens = tokens;
        this.jwt = jwt;
    }

    /** Starts a new session (login). Returns the refresh JWT. */
    @Transactional
    public String startSession(Users u) {
        return issue(u, UUID.randomUUID().toString());
    }

    /** Issues the next token of an existing session (used after {@link #rotate}). */
    @Transactional
    public String continueSession(Users u, String familyId) {
        return issue(u, familyId);
    }

    private String issue(Users u, String familyId) {
        String jti = UUID.randomUUID().toString();
        Instant expires = Instant.now().plusMillis(jwt.getRefreshExpiresMs());
        tokens.save(new RefreshToken(jti, u.getId(), familyId, expires));
        return jwt.generateRefreshToken(u.getId(), u.getRole().name(), jti);
    }

    /**
     * Validates and consumes a refresh token. A token can be used exactly once.
     * noRollbackFor: when reuse is detected we still want the family revocation to be saved.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public Rotation rotate(String rawToken) {
        Claims claims;
        try {
            claims = jwt.parseRefreshToken(rawToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw invalid();
        }
        String jti = claims.getId();
        if (jti == null) throw invalid();            // token from before revocation existed: log in again

        RefreshToken row = tokens.findById(jti).orElseThrow(RefreshTokenService::invalid);
        Instant now = Instant.now();
        if (row.getExpiresAt().isBefore(now)) throw invalid();

        if (tokens.revokeIfActive(jti, now) == 0) {
            tokens.revokeFamily(row.getFamilyId(), now);   // reuse detected: end the whole session
            throw invalid();
        }
        return new Rotation(row.getUserId(), row.getFamilyId());
    }

    /** Logout: ends the session this token belongs to. Never fails: a bad or expired token is ignored. */
    @Transactional
    public void revokeSession(String rawToken) {
        String jti;
        try {
            jti = jwt.parseRefreshToken(rawToken).getId();
        } catch (ExpiredJwtException e) {
            jti = e.getClaims().getId();
        } catch (JwtException | IllegalArgumentException e) {
            return;
        }
        if (jti == null) return;
        tokens.findById(jti).ifPresent(t -> tokens.revokeFamily(t.getFamilyId(), Instant.now()));
    }

    /** Ends every session of a user (logout everywhere, password reset). */
    @Transactional
    public void revokeAll(String userId) {
        tokens.revokeAllForUser(userId, Instant.now());
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
    }
}