package com.lostandfound.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Body of POST /v1/auth/resend-verification. */
public record ResendVerificationRequest(@NotBlank @Email String email) {}