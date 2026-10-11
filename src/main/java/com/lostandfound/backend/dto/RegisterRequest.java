package com.lostandfound.backend.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Positive Integer studentId,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 11, max = 11, message = "Phone Number must have 11 characters") String phoneNum,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
) {}