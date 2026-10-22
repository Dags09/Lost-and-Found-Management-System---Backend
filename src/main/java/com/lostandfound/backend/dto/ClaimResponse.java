package com.lostandfound.backend.dto;

import com.lostandfound.backend.config.ImageUrls;
import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.DropOrClaimed;
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
        Instant reviewedAt,
        Pickup pickup
) {

    public record Pickup(String placeName, String personName, String contact) {
        static Pickup from(DropOrClaimed d) {
            return d == null ? null : new Pickup(d.getName(), d.getPersonName(), d.getContact());
        }
    }


    public static ClaimResponse from(Claim c) {
        return from(c, null);
    }


    public static ClaimResponse from(Claim c, DropOrClaimed dropOff) {
        Pickup pickup = c.getStatus() == ClaimStatus.APPROVED ? Pickup.from(dropOff) : null;
        return new ClaimResponse(
                c.getId(), c.getItemId(), c.getProofDescription(),
                ImageUrls.forClaimProof(c.getId(), c.getProofImageId()),
                c.getStatus(), c.getReviewNote(), c.getCreatedAt(), c.getReviewedAt(), pickup);
    }
}