package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;

import java.time.Instant;
import java.time.LocalDate;

public record ItemPublicResponse(
        String id,
        String name,
        ItemType type,
        String description,
        String categoryId,
        String color,
        String brand,
        String locationName,
        LocalDate dateOccurred,
        ItemStatus status,
        Instant createdAt
) {
    public static ItemPublicResponse from(Items i) {
        return new ItemPublicResponse(
                i.getId(), i.getName(), i.getType(), i.getDescription(),
                i.getCategoryId(), i.getColor(), i.getBrand(),
                i.getLocationName(), i.getDateOccurred(), i.getStatus(), i.getCreatedAt());
    }
}