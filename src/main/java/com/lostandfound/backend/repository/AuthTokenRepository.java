package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/** Database access for one-time email tokens (verify email, reset password). */
public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {

    /** Looks a token up by the SHA-256 hash of the raw value from the email link. */
    Optional<AuthToken> findByTokenHash(String tokenHash);

    /** Atomically marks a token as used. Returns 0 if it was already used or has expired. */
    @Modifying(clearAutomatically = true)
    @Query("update AuthToken t set t.usedAt = :now where t.id = :id and t.usedAt is null and t.expiresAt > :now")
    int markUsed(@Param("id") String id, @Param("now") Instant now);

    /** Invalidates older unused tokens of the same kind, so only the newest link works. */
    @Modifying(clearAutomatically = true)
    @Query("update AuthToken t set t.usedAt = :now where t.userId = :userId and t.type = :type and t.usedAt is null")
    int invalidateUnused(@Param("userId") String userId, @Param("type") AuthToken.Type type,
                         @Param("now") Instant now);

    /** Housekeeping for AuthCleanupJob. */
    @Modifying
    @Query("delete from AuthToken t where t.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}