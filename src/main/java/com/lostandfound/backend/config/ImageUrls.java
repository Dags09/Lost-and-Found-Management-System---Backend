package com.lostandfound.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * Builds the image URLs placed in API responses. Static so DTO records can call it without
 * injection. If app.public-url is set it is put in front, giving absolute links.
 */
@Component
public class ImageUrls {

    private static volatile String base = "";

    public ImageUrls(@Value("${app.public-url:}") String publicUrl) {
        base = publicUrl == null ? "" : publicUrl.replaceAll("/+$", "");
    }

    /** Public URL of a found-item photo. */
    public static String forItemImage(String imageId) {
        return imageId == null ? null : base + "/v1/images/" + imageId;
    }


    /** Login-protected URL of a lost-report photo (owner and staff only). */
    public static String forLostImage(String itemId, int position) {
        return itemId == null ? null : base + "/v1/lost-items/" + itemId + "/images/" + position;
    }

    /** Login-protected URL of a claim's proof photo (claimant and staff only). */
    public static String forClaimProof(String claimId, String proofImageId) {
        return proofImageId == null ? null : base + "/v1/claims/" + claimId + "/proof";
    }
}