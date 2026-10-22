package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Match;
import com.lostandfound.backend.models.enumRoleStatusTypes.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Database access for lost/found match suggestions. */
public interface MatchRepository extends JpaRepository<Match, String> {
    /** Stops the same pair from being suggested twice (also enforced by ux_matches_pair). */
    boolean existsByLostItemIdAndFoundItemId(String lostItemId, String foundItemId);

    /** A lost report's suggestions, best score first. */
    List<Match> findByLostItemIdAndStatusOrderByScoreDesc(String lostItemId, MatchStatus status);

    /** Suggestions pointing at one found item (used when that item is claimed). */
    List<Match> findByFoundItemIdAndStatus(String foundItemId, MatchStatus status);

    /** Looks a match up only within the given lost report, so you cannot dismiss someone else's. */
    Optional<Match> findByIdAndLostItemId(String id, String lostItemId);
}