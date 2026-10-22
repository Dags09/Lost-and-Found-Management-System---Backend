package com.lostandfound.backend.dto;

import com.lostandfound.backend.config.ImageUrls;
import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.DropOrClaimed;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;

import java.time.Instant;

/** What staff see when reviewing: the proof next to the item's SECRET marks. Staff endpoints only. */
public record StaffClaimResponse(
        String id,
        ClaimStatus status,
        String proofDescription,
        String proofImageUrl,
        String reviewNote,
        String reviewedBy,
        Instant createdAt,
        Instant reviewedAt,
        ClaimantInfo claimant,
        ItemInfo item
) {
    public record ClaimantInfo(String id, String name, String studentId, String email, String phoneNum) {
        static ClaimantInfo from(Users u) {
            return u == null ? null : new ClaimantInfo(u.getId(),
                    u.getFirstName() + " " + u.getLastName(), u.getStudentId(), u.getEmail(), u.getPhoneNum());
        }
    }

    public record ItemInfo(String id, String name, String description, String color, String brand,
                           String distinguishingMarks, String locationName, ItemStatus status,
                           String dropOffPlace, String dropOffPerson) {
        static ItemInfo from(Items i, DropOrClaimed d) {
            return i == null ? null : new ItemInfo(i.getId(), i.getName(), i.getDescription(), i.getColor(),
                    i.getBrand(), i.getDistinguishingMarks(), i.getLocationName(), i.getStatus(),
                    d == null ? null : d.getName(), d == null ? null : d.getPersonName());
        }
    }

    public static StaffClaimResponse from(Claim c, Users claimant, Items item, DropOrClaimed dropOff) {
        return new StaffClaimResponse(
                c.getId(), c.getStatus(), c.getProofDescription(),
                ImageUrls.forClaimProof(c.getId(), c.getProofImageId()),
                c.getReviewNote(), c.getReviewedBy(), c.getCreatedAt(), c.getReviewedAt(),
                ClaimantInfo.from(claimant), ItemInfo.from(item, dropOff));
    }
}