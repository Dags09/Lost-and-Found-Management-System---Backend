package com.lostandfound.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ImageUrls {

    private static volatile String base = "";

    public ImageUrls(@Value("${app.public-url:}") String publicUrl) {
        base = publicUrl == null ? "" : publicUrl.replaceAll("/+$", "");
    }

    public static String forItemImage(String imageId) {
        return imageId == null ? null : base + "/v1/images/" + imageId;
    }


    public static String forLostImage(String itemId, int position) {
        return itemId == null ? null : base + "/v1/lost-items/" + itemId + "/images/" + position;
    }

    public static String forClaimProof(String claimId, String proofImageId) {
        return proofImageId == null ? null : base + "/v1/claims/" + claimId + "/proof";
    }
}