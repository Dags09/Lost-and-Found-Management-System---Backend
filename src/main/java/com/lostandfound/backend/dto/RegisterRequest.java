package com.lostandfound.backend.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Pattern(regexp = "^\\d{4,20}$", message = "Student ID must be digits only") String studentId,
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(regexp = "^09\\d{9}$", message = "Phone number must be exactly 11 digits")
        String phoneNum,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String password
) {}