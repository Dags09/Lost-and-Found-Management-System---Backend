package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.*;
import com.lostandfound.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * /v1/auth: register, login, refresh, logout, email verification, password reset and the
 * logged-in user's profile. Public except /me and /logout-all (see SecurityConfig).
 */
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    /** POST /v1/auth/register : public sign-up (Student). Sends a verification email; no tokens yet. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    /** POST /v1/auth/login : access + refresh token. 403 until the email is verified, 429 while locked. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    /** POST /v1/auth/refresh : exchange a refresh token for a new pair (each token works once). */
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return auth.refresh(req);
    }

    /** Ends the session of the given refresh token. */
    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody LogoutRequest req) {
        return auth.logout(req);
    }

    /** Ends every session of the logged-in user. */
    @PostMapping("/logout-all")
    public MessageResponse logoutAll(Authentication authentication) {
        if (authentication == null) throw new com.lostandfound.backend.exception.ApiException(HttpStatus.UNAUTHORIZED, "Not logged in");
        return auth.logoutAll((String) authentication.getPrincipal());
    }

    /** POST /v1/auth/verify-email : redeem the token from the verification email. */
    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        return auth.verifyEmail(req);
    }

    /** POST /v1/auth/resend-verification : send a new verification link. */
    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest req) {
        return auth.resendVerification(req);
    }

    /** POST /v1/auth/forgot-password : email a reset link (same answer for unknown emails). */
    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return auth.forgotPassword(req);
    }

    /** POST /v1/auth/reset-password : set a new password with the emailed token. */
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return auth.resetPassword(req);
    }

    /** GET /v1/auth/me : the logged-in user's profile. */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        if (authentication == null) throw new com.lostandfound.backend.exception.ApiException(HttpStatus.UNAUTHORIZED, "Not logged in");
        return auth.me((String) authentication.getPrincipal());
    }
}