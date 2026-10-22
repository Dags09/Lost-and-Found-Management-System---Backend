package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for creating or editing a drop-off location (Admin only). */
public record DropLocationRequest(@NotBlank String name,
                                  @NotBlank String personName,
                                  @NotBlank String contact) {}