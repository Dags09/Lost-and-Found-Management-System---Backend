package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * Body for reporting a FOUND item (POST /v1/items) and for editing it. distinguishingMarks is
 * private: it is only used to check owners and is never returned in public responses.
 */
public record ItemCreateRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String categoryId,
        @NotBlank String color,
        @NotBlank String brand,
        @NotBlank String distinguishingMarks,
        @NotBlank String locationName,
        @NotBlank String dropLocationId,
        @NotNull @PastOrPresent LocalDate dateOccurred
) {}