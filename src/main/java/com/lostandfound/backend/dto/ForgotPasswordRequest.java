package com.lostandfound.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Body of POST /v1/auth/forgot-password: the email that should get a reset link. */
public record ForgotPasswordRequest(@NotBlank @Email String email) {}