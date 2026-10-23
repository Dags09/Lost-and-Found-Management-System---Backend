package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Body of PUT /v1/auth/me. Only the name and phone number can be changed. Email, student ID, role and
 * password have their own flows (or are fixed), so they are not accepted here.
 * Phone: 11 digits starting with 09 (same rule as sign-up).
 */
public record UpdateProfileRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank
        @Pattern(regexp = "^09\\d{9}$", message = "Phone number must be exactly 11 digits")
        String phoneNum
) {}