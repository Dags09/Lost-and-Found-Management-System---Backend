package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Database access for claims. The method names are the queries (Spring Data builds them). */
public interface ClaimRepository extends JpaRepository<Claim, String> {
    /** Used to stop a student from having two pending claims on the same item. */
    boolean existsByItemIdAndClaimantIdAndStatus(String itemId, String claimantId, ClaimStatus status);
    /** True if anyone has a claim with this status on the item (e.g. a PENDING one blocks edits). */
    boolean existsByItemIdAndStatus(String itemId, ClaimStatus status);
    /** A student's own claims, newest first. */
    List<Claim> findByClaimantIdOrderByCreatedAtDesc(String claimantId);
    /** Every claim on an item (used when deleting the item and its proof photos). */
    List<Claim> findByItemId(String itemId);
    /** The staff queue: oldest first. */
    List<Claim> findByStatusOrderByCreatedAtAsc(ClaimStatus status);
    /** Used to reject the other pending claims when one is approved. */
    List<Claim> findByItemIdAndStatus(String itemId, ClaimStatus status);
}