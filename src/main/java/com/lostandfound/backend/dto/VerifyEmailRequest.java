package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /v1/auth/verify-email. token comes from the link in the verification email. */
public record VerifyEmailRequest(@NotBlank String token) {}