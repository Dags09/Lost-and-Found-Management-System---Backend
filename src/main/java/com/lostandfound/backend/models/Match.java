package com.lostandfound.backend.models;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.lostandfound.backend.models.enumRoleStatusTypes.MatchStatus;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

/**
 * A suggested pairing of a lost report and a found item, with a score from MatchScorer (0 to 1).
 * It only suggests: the owner still has to submit a claim with proof.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(name = "lost_item_id")
    private String lostItemId;

    @Column(name = "found_item_id")
    private String foundItemId;

    private double score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SUGGESTED;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    public Match() {}


    // Getters & Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLostItemId() { return lostItemId; }
    public void setLostItemId(String lostItemId) { this.lostItemId = lostItemId; }

    public String getFoundItemId() { return foundItemId; }
    public void setFoundItemId(String foundItemId) { this.foundItemId = foundItemId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

}