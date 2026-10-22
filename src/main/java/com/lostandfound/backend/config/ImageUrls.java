package com.lostandfound.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Builds the URLs clients use to load pictures. Set APP_PUBLIC_URL (e.g. https://api.example.com)
 * to get absolute URLs; leave it empty to get paths like /v1/images/{id}.
 */
@Component
public class ImageUrls {

    private static volatile String base = "";

    public ImageUrls(@Value("${app.public-url:}") String publicUrl) {
        base = publicUrl == null ? "" : publicUrl.replaceAll("/+$", "");
    }

    public static String forItemImage(String imageId) {
        return imageId == null ? null : base + "/v1/images/" + imageId;
    }

    /** Lost-report photos are private, so they are served by the lost-report routes, not /v1/images. */
    public static String forLostImage(String itemId, int position) {
        return itemId == null ? null : base + "/v1/lost-items/" + itemId + "/images/" + position;
    }

    public static String forClaimProof(String claimId, String proofImageId) {
        return proofImageId == null ? null : base + "/v1/claims/" + claimId + "/proof";
    }
}