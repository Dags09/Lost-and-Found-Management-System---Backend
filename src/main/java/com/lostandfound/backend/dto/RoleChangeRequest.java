package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(@NotNull Role role) {}