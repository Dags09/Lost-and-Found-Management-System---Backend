package com.lostandfound.backend.service;

import com.lostandfound.backend.repository.AuthTokenRepository;
import com.lostandfound.backend.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/** Nightly: deletes expired refresh tokens and email tokens so the tables stay small. */
@Component
public class AuthCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(AuthCleanupJob.class);

    private final RefreshTokenRepository refreshTokens;
    private final AuthTokenRepository authTokens;

    public AuthCleanupJob(RefreshTokenRepository refreshTokens, AuthTokenRepository authTokens) {
        this.refreshTokens = refreshTokens;
        this.authTokens = authTokens;
    }

    @Scheduled(cron = "${app.auth.cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void purge() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(1));
        int a = refreshTokens.deleteExpiredBefore(cutoff);
        int b = authTokens.deleteExpiredBefore(cutoff);
        log.info("Auth cleanup: removed {} refresh tokens and {} email tokens", a, b);
    }
}