package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;

import java.time.Instant;
import java.time.LocalDate;

public record MyItemResponse(
        String id,
        String name,
        ItemType type,
        String description,
        String categoryId,
        String color,
        String brand,
        String distinguishingMarks,
        String locationName,
        String dropLocationId,
        LocalDate dateOccurred,
        ItemStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static MyItemResponse from(Items i) {
        return new MyItemResponse(
                i.getId(), i.getName(), i.getType(), i.getDescription(),
                i.getCategoryId(), i.getColor(), i.getBrand(),
                i.getDistinguishingMarks(), i.getLocationName(), i.getDropLocationId(),
                i.getDateOccurred(), i.getStatus(), i.getCreatedAt(), i.getUpdatedAt());
    }
}