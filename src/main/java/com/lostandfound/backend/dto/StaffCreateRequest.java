package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import jakarta.validation.constraints.*;


public record StaffCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(regexp = "^09\\d{9}$", message = "Phone number must be exactly 11 digits")
        String phoneNum,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String password,
        @NotNull Role role
) {}