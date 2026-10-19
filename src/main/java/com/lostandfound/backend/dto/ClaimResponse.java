package com.lostandfound.backend.dto;

import com.lostandfound.backend.config.ImageUrls;
import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;

import java.time.Instant;

public record ClaimResponse(
        String id,
        String itemId,
        String proofDescription,
        String proofImageUrl,
        ClaimStatus status,
        String reviewNote,
        Instant createdAt,
        Instant reviewedAt
) {
    public static ClaimResponse from(Claim c) {
        return new ClaimResponse(
                c.getId(), c.getItemId(), c.getProofDescription(), ImageUrls.forClaimProof(c.getId(), c.getProofImageId()),
                c.getStatus(), c.getReviewNote(), c.getCreatedAt(), c.getReviewedAt());
    }
}