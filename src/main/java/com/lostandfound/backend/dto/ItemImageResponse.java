package com.lostandfound.backend.dto;

import com.lostandfound.backend.config.ImageUrls;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;

import java.util.List;

/**
 * One photo of an item as the API returns it: a URL the client can load, plus its position
 * (0 = first). Lost-report photos get a login-protected URL, found-item photos the public one.
 */
public record ItemImageResponse(String url, int position) {
    public static List<ItemImageResponse> listFrom(Items i) {
        boolean lost = i.getType() == ItemType.LOST;
        return i.getImages().stream()
                .filter(img -> img.getImageId() != null)
                .map(img -> new ItemImageResponse(
                        lost ? ImageUrls.forLostImage(i.getId(), img.getPosition())
                                : ImageUrls.forItemImage(img.getImageId()),
                        img.getPosition()))
                .toList();
    }
}