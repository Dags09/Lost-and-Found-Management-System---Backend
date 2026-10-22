package com.lostandfound.backend.dto;

import com.lostandfound.backend.config.ImageUrls;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;

import java.util.List;

public record ItemImageResponse(String url, int position) {
    public static List<ItemImageResponse> listFrom(Items i) {
        boolean lost = i.getType() == ItemType.LOST;   // lost-report photos are private: different URL
        return i.getImages().stream()
                .filter(img -> img.getImageId() != null)   // skips old Cloudinary-era rows
                .map(img -> new ItemImageResponse(
                        lost ? ImageUrls.forLostImage(i.getId(), img.getPosition())
                                : ImageUrls.forItemImage(img.getImageId()),
                        img.getPosition()))
                .toList();
    }
}