package com.lostandfound.backend.dto;

import com.lostandfound.backend.config.ImageUrls;
import com.lostandfound.backend.models.Items;

import java.util.List;

public record ItemImageResponse(String url, int position) {
    public static List<ItemImageResponse> listFrom(Items i) {
        return i.getImages().stream()
                .filter(img -> img.getImageId() != null)   // skips old Cloudinary-era rows
                .map(img -> new ItemImageResponse(ImageUrls.forItemImage(img.getImageId()), img.getPosition()))
                .toList();
    }
}