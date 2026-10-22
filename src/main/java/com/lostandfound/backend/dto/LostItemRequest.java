package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * "I lost something". No drop-off point (you don't have the item) and no photo yet.
 * distinguishingMarks stays private: only you and staff ever see it.
 */
public record LostItemRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String categoryId,
        @NotBlank String color,
        @NotBlank String brand,
        @NotBlank String distinguishingMarks,
        @NotBlank String locationName,          // where you think you lost it
        @NotNull @PastOrPresent LocalDate dateOccurred
) {}