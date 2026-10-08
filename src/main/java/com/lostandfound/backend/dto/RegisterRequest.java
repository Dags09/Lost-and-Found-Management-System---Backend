package com.lostandfound.backend.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Positive Integer studentId,
        @NotBlank @Email String email,
        @NotBlank String phoneNum,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
) {}