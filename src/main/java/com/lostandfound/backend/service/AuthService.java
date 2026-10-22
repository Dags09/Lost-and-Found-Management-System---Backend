package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.*;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.mail.EmailService;
import com.lostandfound.backend.models.AuthToken;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import com.lostandfound.backend.repository.UserRepository;
import com.lostandfound.backend.security.JwtService;
import com.lostandfound.backend.security.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private static final int VERIFY_HOURS = 24;
    private static final int RESET_MINUTES = 60;
    private static final long HOUR_MS = 3_600_000;

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final RefreshTokenService refreshTokens;
    private final AuthTokenService authTokens;
    private final EmailService emails;
    private final RateLimiter limiter;
    private final String frontendUrl;
    private final int maxFailedLogins;
    private final Duration lockDuration;
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt,
                       RefreshTokenService refreshTokens, AuthTokenService authTokens,
                       EmailService emails, RateLimiter limiter,
                       @Value("${app.frontend-url}") String frontendUrl,
                       @Value("${app.auth.max-failed-logins:5}") int maxFailedLogins,
                       @Value("${app.auth.lock-minutes:15}") int lockMinutes) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.refreshTokens = refreshTokens;
        this.authTokens = authTokens;
        this.emails = emails;
        this.limiter = limiter;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.maxFailedLogins = maxFailedLogins;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
        this.dummyHash = encoder.encode("anything");
    }

    // ---- sign up / verify ------------------------------------------------

    /**
     * Public sign-up always creates a Student. Staff accounts are created by an Admin.
     * The account cannot log in until the email is verified, so no tokens are returned here
     * (accessToken and refreshToken are null). A verification email is sent instead.
     */
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)
                || users.existsByPhoneNum(req.phoneNum())
                || users.existsByStudentId(req.studentId()))
            throw new ApiException(HttpStatus.CONFLICT, "An account with these details already exists");

        Users u = new Users();
        u.setFirstName(req.firstName().trim());
        u.setLastName(req.lastName().trim());
        u.setStudentId(req.studentId());
        u.setEmail(email);
        u.setPhoneNum(req.phoneNum());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.Student);
        u.setEmailVerified(false);
        u = users.save(u);

        sendVerification(u);
        return new AuthResponse(null, null, UserResponse.from(u));
    }

    /** Marks the email as verified when given a valid, unused, unexpired token. */
    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest req) {
        String userId = authTokens.consume(req.token(), AuthToken.Type.VERIFY_EMAIL)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "This link is invalid or has expired. Request a new one."));
        Users u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "This link is invalid or has expired."));
        u.setEmailVerified(true);
        users.save(u);
        return new MessageResponse("Email verified. You can now log in.");
    }

    /** Always answers the same way, so it cannot be used to find out which emails are registered. */
    public MessageResponse resendVerification(ResendVerificationRequest req) {
        String email = req.email().trim().toLowerCase();
        if (withinEmailLimit("resend", email)) {
            users.findByEmail(email).filter(u -> !u.isEmailVerified()).ifPresent(this::sendVerification);
        }
        return new MessageResponse("If that account exists and is not verified, we sent a new link.");
    }

    // ---- login / sessions ------------------------------------------------

    public AuthResponse login(LoginRequest req) {
        Users u = users.findByEmail(req.email().trim().toLowerCase()).orElse(null);
        Instant now = Instant.now();

        // Locked: still run a hash so the response time does not reveal the lock.
        if (u != null && u.getLockedUntil() != null && u.getLockedUntil().isAfter(now)) {
            encoder.matches(req.password(), dummyHash);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Try again later or reset your password.");
        }

        boolean ok = encoder.matches(req.password(), u != null ? u.getPassword() : dummyHash);
        if (u == null || !ok) {
            if (u != null) recordFailedLogin(u, now);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (u.getFailedLoginAttempts() > 0 || u.getLockedUntil() != null) {
            u.setFailedLoginAttempts(0);
            u.setLockedUntil(null);
            users.save(u);
        }
        // Only said after the password was right, so it does not leak which emails exist.
        if (!u.isEmailVerified())
            throw new ApiException(HttpStatus.FORBIDDEN, "Please verify your email first. Check your inbox.");

        return tokensFor(u);
    }

    private void recordFailedLogin(Users u, Instant now) {
        int attempts = u.getFailedLoginAttempts() + 1;
        if (attempts >= maxFailedLogins) {
            u.setLockedUntil(now.plus(lockDuration));
            attempts = 0;
        }
        u.setFailedLoginAttempts(attempts);
        users.save(u);
    }

    /** Each refresh token works once. Reusing one ends the whole session (see RefreshTokenService). */
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse refresh(RefreshRequest req) {
        RefreshTokenService.Rotation r = refreshTokens.rotate(req.refreshToken());
        Users u = users.findById(r.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User no longer exists"));
        return new AuthResponse(jwt.generateAccessToken(u.getId(), u.getRole().name()),
                refreshTokens.continueSession(u, r.familyId()), UserResponse.from(u));
    }

    /** Ends the session of this refresh token. Always succeeds. */
    public MessageResponse logout(LogoutRequest req) {
        refreshTokens.revokeSession(req.refreshToken());
        return new MessageResponse("Logged out");
    }

    /** Ends every session of the user on every device. */
    public MessageResponse logoutAll(String userId) {
        refreshTokens.revokeAll(userId);
        return new MessageResponse("Logged out everywhere");
    }

    public UserResponse me(String userId) {
        return users.findById(userId).map(UserResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    // ---- password reset --------------------------------------------------

    /** Always answers the same way, so it cannot be used to find out which emails are registered. */
    public MessageResponse forgotPassword(ForgotPasswordRequest req) {
        String email = req.email().trim().toLowerCase();
        if (withinEmailLimit("forgot", email)) {
            users.findByEmail(email).ifPresent(u -> {
                String raw = authTokens.create(u.getId(), AuthToken.Type.RESET_PASSWORD, Duration.ofMinutes(RESET_MINUTES));
                emails.sendPasswordResetEmail(u.getEmail(), u.getFirstName(),
                        frontendUrl + "/reset-password?token=" + raw, RESET_MINUTES);
            });
        }
        return new MessageResponse("If that account exists, we sent a link to reset the password.");
    }

    /**
     * Sets a new password, then logs the user out everywhere (so a thief's session dies too)
     * and clears any login lock. Following the emailed link also proves the email is theirs.
     */
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest req) {
        String userId = authTokens.consume(req.token(), AuthToken.Type.RESET_PASSWORD)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "This link is invalid or has expired. Request a new one."));
        Users u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "This link is invalid or has expired."));
        u.setPassword(encoder.encode(req.newPassword()));
        u.setEmailVerified(true);
        u.setFailedLoginAttempts(0);
        u.setLockedUntil(null);
        users.save(u);
        refreshTokens.revokeAll(u.getId());
        return new MessageResponse("Password changed. Please log in with your new password.");
    }

    // ---- helpers ---------------------------------------------------------

    /** At most 3 emails per address per hour, so nobody can use us to flood someone's inbox. */
    private boolean withinEmailLimit(String kind, String email) {
        return limiter.tryAcquire(kind + ":" + email, 3, HOUR_MS) == 0;
    }

    private void sendVerification(Users u) {
        String raw = authTokens.create(u.getId(), AuthToken.Type.VERIFY_EMAIL, Duration.ofHours(VERIFY_HOURS));
        emails.sendVerificationEmail(u.getEmail(), u.getFirstName(),
                frontendUrl + "/verify-email?token=" + raw, VERIFY_HOURS);
    }

    private AuthResponse tokensFor(Users u) {
        return new AuthResponse(jwt.generateAccessToken(u.getId(), u.getRole().name()),
                refreshTokens.startSession(u), UserResponse.from(u));
    }
}