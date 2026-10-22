package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /v1/auth/logout: the refresh token of the session to end. */
public record LogoutRequest(@NotBlank String refreshToken) {}