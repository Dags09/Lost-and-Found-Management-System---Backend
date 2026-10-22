package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for creating or editing a category (Admin only). */
public record CategoryRequest(@NotBlank String name, @NotBlank String icon) {}