package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, String> {
    boolean existsByItemIdAndClaimantIdAndStatus(String itemId, String claimantId, ClaimStatus status);
    boolean existsByItemIdAndStatus(String itemId, ClaimStatus status);
    List<Claim> findByClaimantIdOrderByCreatedAtDesc(String claimantId);
    List<Claim> findByItemId(String itemId);
    List<Claim> findByStatusOrderByCreatedAtAsc(ClaimStatus status);
    List<Claim> findByItemIdAndStatus(String itemId, ClaimStatus status);
}