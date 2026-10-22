package com.lostandfound.backend.service;

import com.lostandfound.backend.models.AuthToken;
import com.lostandfound.backend.repository.AuthTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;


/**
 * Creates and redeems one-time email tokens (verify email, reset password). Only a SHA-256 hash
 * is stored, so a leaked database cannot be used to activate or reset accounts.
 */
@Service
public class AuthTokenService {

    private final AuthTokenRepository tokens;
    private final SecureRandom random = new SecureRandom();

    public AuthTokenService(AuthTokenRepository tokens) {
        this.tokens = tokens;
    }

    /**
     * Returns the raw token to put in the email link (only its hash is stored). Older unused tokens of
     * the same kind stop working, so only the newest link is valid.
     */
    @Transactional
    public String create(String userId, AuthToken.Type type, Duration ttl) {
        Instant now = Instant.now();
        tokens.invalidateUnused(userId, type, now);

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new AuthToken(userId, type, hash(raw), now.plus(ttl)));
        return raw;
    }

    /** Redeems a token once. Returns the user id, or empty if the token is unknown, wrong kind, used or expired. */
    @Transactional
    public Optional<String> consume(String raw, AuthToken.Type type) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        return tokens.findByTokenHash(hash(raw.trim()))
                .filter(t -> t.getType() == type)
                .filter(t -> tokens.markUsed(t.getId(), Instant.now()) == 1)
                .map(AuthToken::getUserId);
    }

    /** SHA-256 hex of the token. The raw token is high-entropy random data, so a fast hash is enough. */
    private static String hash(String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}