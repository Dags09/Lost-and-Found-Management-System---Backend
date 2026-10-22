package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.StaffClaimResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.DropOrClaimed;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import com.lostandfound.backend.repository.ClaimRepository;
import com.lostandfound.backend.repository.DropLocationRepository;
import com.lostandfound.backend.repository.ItemRepository;
import com.lostandfound.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClaimReviewService {

    private final ClaimRepository claims;
    private final ItemRepository items;
    private final UserRepository users;
    private final DropLocationRepository dropLocations;
    private final NotificationService notifications;
    private final MatchService matching;

    public ClaimReviewService(ClaimRepository claims, ItemRepository items,
                              UserRepository users, DropLocationRepository dropLocations,
                              NotificationService notifications, MatchService matching) {
        this.claims = claims;
        this.items = items;
        this.users = users;
        this.dropLocations = dropLocations;
        this.notifications = notifications;
        this.matching = matching;
    }

    /** The review queue, oldest first. Three bulk lookups, not one per claim. */
    @Transactional(readOnly = true)
    public List<StaffClaimResponse> list(ClaimStatus status) {
        return toStaffResponses(claims.findByStatusOrderByCreatedAtAsc(status));
    }

    /**
     * Approve: the item must still be OPEN. Claim -> APPROVED, item -> CLAIMED, and every other
     * pending claim on the item is rejected. All in ONE transaction: it all happens or none of it does.
     */
    @Transactional
    public StaffClaimResponse approve(String claimId, String staffId, String note) {
        Claim c = loadPendingClaim(claimId, staffId);

        if (items.markClaimedIfOpen(c.getItemId(), Instant.now()) == 0)
            throw new ApiException(HttpStatus.CONFLICT, "This item is no longer open");

        Instant now = Instant.now();
        finish(c, ClaimStatus.APPROVED, staffId, clean(note), now);

        Items item = items.findById(c.getItemId()).orElse(null);
        String itemName = item == null ? "the item" : item.getName();

        // Tell the owner, and tell the finder their item found its owner.
        notifications.claimUpdate(c.getClaimantId(), "CLAIM_APPROVED",
                "Your claim for \"" + itemName + "\" was approved. Open it to see where to collect the item.",
                c.getItemId(), c.getId());
        if (item != null)
            notifications.claimUpdate(item.getReporterId(), "ITEM_CLAIMED",
                    "The owner of \"" + itemName + "\" has been found. Thank you for reporting it!",
                    c.getItemId(), null);

        // If the owner had a lost report for this item, it is solved: close it, settle the matches.
        matching.settleAfterApproval(c.getItemId(), c.getClaimantId());

        for (Claim other : claims.findByItemIdAndStatus(c.getItemId(), ClaimStatus.PENDING)) {
            if (!other.getId().equals(c.getId())) {
                finish(other, ClaimStatus.REJECTED, staffId, "Another claim for this item was approved", now);
                notifications.claimUpdate(other.getClaimantId(), "CLAIM_REJECTED",
                        "Your claim for \"" + itemName + "\" was not approved: another claim for this item was approved.",
                        other.getItemId(), other.getId());
            }
        }
        return toStaffResponses(List.of(c)).get(0);
    }

    /** Reject: a reason is required, so the student knows what to fix. The item stays OPEN. */
    @Transactional
    public StaffClaimResponse reject(String claimId, String staffId, String note) {
        String reason = clean(note);
        if (reason == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Give a reason for rejecting this claim");
        Claim c = loadPendingClaim(claimId, staffId);
        finish(c, ClaimStatus.REJECTED, staffId, reason, Instant.now());

        String itemName = items.findById(c.getItemId()).map(Items::getName).orElse("the item");
        notifications.claimUpdate(c.getClaimantId(), "CLAIM_REJECTED",
                "Your claim for \"" + itemName + "\" was rejected: " + reason,
                c.getItemId(), c.getId());
        return toStaffResponses(List.of(c)).get(0);
    }

    // ---- helpers -------------------------------------------------------

    /** 404 not found, 409 already reviewed, 403 your own claim. */
    private Claim loadPendingClaim(String claimId, String staffId) {
        Claim c = claims.findById(claimId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Claim not found"));
        if (c.getStatus() != ClaimStatus.PENDING)
            throw new ApiException(HttpStatus.CONFLICT, "This claim was already reviewed");
        if (staffId.equals(c.getClaimantId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot review your own claim");
        return c;
    }

    private void finish(Claim c, ClaimStatus status, String staffId, String note, Instant at) {
        c.setStatus(status);
        c.setReviewedBy(staffId);
        c.setReviewNote(note);
        c.setReviewedAt(at);
        claims.save(c);
    }

    private static String clean(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private List<StaffClaimResponse> toStaffResponses(List<Claim> list) {
        if (list.isEmpty()) return List.of();

        Map<String, Users> claimants = users.findAllById(
                        list.stream().map(Claim::getClaimantId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Users::getId, Function.identity()));
        Map<String, Items> itemById = items.findAllById(
                        list.stream().map(Claim::getItemId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Items::getId, Function.identity()));
        Map<String, DropOrClaimed> places = dropLocations.findAllById(
                        itemById.values().stream().map(Items::getDropLocationId)
                                .filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(DropOrClaimed::getId, Function.identity()));

        return list.stream().map(c -> {
            Items item = itemById.get(c.getItemId());
            DropOrClaimed place = item == null ? null : places.get(item.getDropLocationId());
            return StaffClaimResponse.from(c, claimants.get(c.getClaimantId()), item, place);
        }).toList();
    }
}