package com.lostandfound.backend.dto;

import com.lostandfound.backend.validation.StrongPassword;
import jakarta.validation.constraints.*;

/**
 * Body of POST /v1/auth/register (students sign themselves up). Student ID: digits only.
 * Phone: 11 digits starting with 09. Password: see @StrongPassword.
 */
public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Pattern(regexp = "^\\d{4,20}$", message = "Student ID must be digits only") String studentId,
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(regexp = "^09\\d{9}$", message = "Phone number must be exactly 11 digits")
        String phoneNum,
        @NotBlank @StrongPassword String password
) {}