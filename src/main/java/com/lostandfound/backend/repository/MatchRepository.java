package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Match;
import com.lostandfound.backend.models.enumRoleStatusTypes.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, String> {
    boolean existsByLostItemIdAndFoundItemId(String lostItemId, String foundItemId);

    List<Match> findByLostItemIdAndStatusOrderByScoreDesc(String lostItemId, MatchStatus status);

    List<Match> findByFoundItemIdAndStatus(String foundItemId, MatchStatus status);

    Optional<Match> findByIdAndLostItemId(String id, String lostItemId);
}