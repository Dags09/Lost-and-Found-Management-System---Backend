package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Match;
import com.lostandfound.backend.models.enumRoleStatusTypes.MatchStatus;

import java.time.Instant;

/**
 * One suggestion shown to the owner of a lost report. The found item is the PUBLIC view:
 * no secret marks and no drop-off point. To get it back, the owner still submits a claim.
 */
public record MatchResponse(
        String id,
        int score,                    // 0-100, higher means more alike
        MatchStatus status,
        Instant createdAt,
        ItemPublicResponse foundItem
) {
    public static MatchResponse from(Match m, ItemPublicResponse foundItem) {
        return new MatchResponse(m.getId(), (int) Math.round(m.getScore() * 100),
                m.getStatus(), m.getCreatedAt(), foundItem);
    }
}