package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import jakarta.validation.constraints.NotNull;

/** Body of PATCH /v1/admin/users/{id}/role (Admin only). */
public record RoleChangeRequest(@NotNull Role role) {}