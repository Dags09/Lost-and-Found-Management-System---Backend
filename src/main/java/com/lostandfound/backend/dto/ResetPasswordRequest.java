package com.lostandfound.backend.dto;

import com.lostandfound.backend.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank String token, @NotBlank @StrongPassword String newPassword) {}