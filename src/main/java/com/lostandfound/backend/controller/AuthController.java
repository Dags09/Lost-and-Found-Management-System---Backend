package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.*;
import com.lostandfound.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

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

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        return auth.verifyEmail(req);
    }

    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest req) {
        return auth.resendVerification(req);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return auth.forgotPassword(req);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return auth.resetPassword(req);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        if (authentication == null) throw new com.lostandfound.backend.exception.ApiException(HttpStatus.UNAUTHORIZED, "Not logged in");
        return auth.me((String) authentication.getPrincipal());
    }
}