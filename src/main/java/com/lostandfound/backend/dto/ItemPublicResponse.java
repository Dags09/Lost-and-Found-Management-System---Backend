package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * A FOUND item as everyone sees it. Leaves out distinguishingMarks and the reporter, so nobody can
 * pass an ownership check just by reading the listing.
 */
public record ItemPublicResponse(
        String id,
        String name,
        ItemType type,
        String description,
        String categoryId,
        String color,
        String brand,
        String locationName,
        String dropLocationId,
        LocalDate dateOccurred,
        ItemStatus status,
        Instant createdAt,
        List<ItemImageResponse> images

) {
    public static ItemPublicResponse from(Items i) {
        return new ItemPublicResponse(
                i.getId(), i.getName(), i.getType(), i.getDescription(),
                i.getCategoryId(), i.getColor(), i.getBrand(),
                i.getLocationName(), i.getDropLocationId(), i.getDateOccurred(),
                i.getStatus(), i.getCreatedAt(), ItemImageResponse.listFrom(i));
    }
}