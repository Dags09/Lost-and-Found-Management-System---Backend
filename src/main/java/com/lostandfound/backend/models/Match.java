package com.lostandfound.backend.models;

import com.lostandfound.backend.models.enumRoleStatusTypes.MatchStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "matches")
public class Match {
    @Id
    private String id;

    @Indexed
    private String lostItemId;

    @Indexed
    private String foundItemId;

    /** Similarity score, 0 to 1. */
    private double score;

    private MatchStatus status = MatchStatus.SUGGESTED;

    @CreatedDate
    private Instant createdAt;

    public Match() {}

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
