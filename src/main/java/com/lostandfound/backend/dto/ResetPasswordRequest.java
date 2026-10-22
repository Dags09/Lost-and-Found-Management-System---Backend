package com.lostandfound.backend.dto;

import com.lostandfound.backend.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

/** Body of POST /v1/auth/reset-password. token comes from the link in the reset email. */
public record ResetPasswordRequest(@NotBlank String token, @NotBlank @StrongPassword String newPassword) {}