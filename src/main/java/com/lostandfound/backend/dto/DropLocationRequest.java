package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record DropLocationRequest(@NotBlank String name,
                                  @NotBlank String personName,
                                  @NotBlank String contact) {}