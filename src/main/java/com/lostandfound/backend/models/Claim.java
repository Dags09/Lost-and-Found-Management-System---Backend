package com.lostandfound.backend.models;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "claims")
public class Claim {

    @Column(name = "review_note", columnDefinition = "text")
    private String reviewNote;
    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "claimant_id")
    private String claimantId;

    @NotBlank(message = "provide a description or distinguishing mark of the item")
    @Column(name = "proof_description", nullable = false, columnDefinition = "text")
    private String proofDescription;

    // Proof photo: a row in the "images" table (purpose PROOF). Served only to the claimant and staff.
    @Column(name = "proof_image_id", length = 36)
    private String proofImageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status = ClaimStatus.PENDING;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @CreatedDate
    @Column(name = "created_at")
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

    public String getProofImageId() { return proofImageId; }
    public void setProofImageId(String proofImageId) { this.proofImageId = proofImageId; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}