package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.*;
import com.lostandfound.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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

    /** Requires a valid access token (this path is under /api/auth but we protect it via the check below). */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        if (authentication == null) throw new com.lostandfound.backend.exception.ApiException(HttpStatus.UNAUTHORIZED, "Not logged in");
        return auth.me((String) authentication.getPrincipal());
    }
}
