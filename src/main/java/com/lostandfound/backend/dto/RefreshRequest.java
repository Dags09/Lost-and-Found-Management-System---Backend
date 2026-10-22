package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /v1/auth/refresh: the refresh token to exchange for a new pair. */
public record RefreshRequest(@NotBlank String refreshToken) {}