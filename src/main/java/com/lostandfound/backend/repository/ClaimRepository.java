package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClaimRepository extends MongoRepository<Claim, String> {
    boolean existsByItemIdAndClaimantIdAndStatus(String itemId, String claimantId, ClaimStatus status);
    boolean existsByItemIdAndStatus(String itemId, ClaimStatus status);
    List<Claim> findByClaimantIdOrderByCreatedAtDesc(String claimantId);
}