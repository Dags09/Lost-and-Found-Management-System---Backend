package com.lostandfound.backend.models;

import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "claims")
public class Claim {

    private String proofImagePublicId;   // Cloudinary id, never sent to clients
    private String reviewNote;           // staff's reason (used in 5b)
    private Instant reviewedAt;          // when staff decided (used in 5b)

    @Id
    private String id;

    @Indexed
    private String itemId;

    @Indexed
    private String claimantId;

    @NotBlank(message = "provide a description or distinguishing mark of the item")
    private String proofDescription;

    @NotBlank(message = "provide an image where your holding/using the item or image of the item along with the  distinguishing mark")
    private String proofImageUrl;

    private ClaimStatus status = ClaimStatus.PENDING;

    private String reviewedBy;

    @CreatedDate
    private Instant createdAt;

    public Claim() {}


    // Getters & Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getClaimantId() { return claimantId; }
    public void setClaimantId(String claimantId) { this.claimantId = claimantId; }

    public String getProofDescription() { return proofDescription; }
    public void setProofDescription(String proofDescription) { this.proofDescription = proofDescription; }

    public String getProofImageUrl() { return proofImageUrl; }
    public void setProofImageUrl(String proofImageUrl) { this.proofImageUrl = proofImageUrl; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getProofImagePublicId() { return proofImagePublicId; }
    public void setProofImagePublicId(String proofImagePublicId) { this.proofImagePublicId = proofImagePublicId; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}