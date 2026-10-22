package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import com.lostandfound.backend.validation.StrongPassword;
import jakarta.validation.constraints.*;


/** Body for an Admin creating a Moderator or Admin account. No student ID is needed for staff. */
public record StaffCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(regexp = "^09\\d{9}$", message = "Phone number must be exactly 11 digits")
        String phoneNum,
        @NotBlank @StrongPassword String password,
        @NotNull Role role
) {}