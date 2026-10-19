package com.lostandfound.backend.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Positive Integer studentId,
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(regexp = "^09\\\\d{9}$", message = "Phone number must be exactly 11 digits")
        String phoneNum,
        @NotBlank @Size(min = 10, max = 72, message = "Password must be 8 to 72 characters") String password
) {}