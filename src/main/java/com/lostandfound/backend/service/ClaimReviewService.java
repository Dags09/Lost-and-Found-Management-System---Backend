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
import com.lostandfound.backend.mail.ClaimEmailEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Staff side of claims: list, approve and reject. Approving closes the item, rejects the other
 * pending claims, settles the owner's lost report and notifies + emails everyone involved.
 */
@Service
public class ClaimReviewService {

    private final ClaimRepository claims;
    private final ItemRepository items;
    private final UserRepository users;
    private final DropLocationRepository dropLocations;
    private final NotificationService notifications;
    private final MatchService matching;
    private final ApplicationEventPublisher events;

    public ClaimReviewService(ClaimRepository claims, ItemRepository items,
                              UserRepository users, DropLocationRepository dropLocations,
                              NotificationService notifications, MatchService matching,
                              ApplicationEventPublisher events) {
        this.events = events;
        this.claims = claims;
        this.items = items;
        this.users = users;
        this.dropLocations = dropLocations;
        this.notifications = notifications;
        this.matching = matching;
    }

    /** Claims with the given status, oldest first, so the longest-waiting one is reviewed first. */
    @Transactional(readOnly = true)
    public List<StaffClaimResponse> list(ClaimStatus status) {
        return toStaffResponses(claims.findByStatusOrderByCreatedAtAsc(status));
    }


    /**
     * Approves a claim in ONE transaction:
     * 1. flips the item OPEN -> CLAIMED (fails with 409 if someone else got there first),
     * 2. settles the claimant's lost report and match suggestions,
     * 3. rejects every other pending claim for the item,
     * 4. notifies and emails the people involved.
     * If any step fails, everything rolls back.
     */
    @Transactional
    public StaffClaimResponse approve(String claimId, String staffId, String note) {
        Claim c = loadPendingClaim(claimId, staffId);

        // One atomic UPDATE: if two staff approve different claims at the same moment, only one wins.
        if (items.markClaimedIfOpen(c.getItemId(), Instant.now()) == 0)
            throw new ApiException(HttpStatus.CONFLICT, "This item is no longer open");

        Instant now = Instant.now();
        finish(c, ClaimStatus.APPROVED, staffId, clean(note), now);

        Items item = items.findById(c.getItemId()).orElse(null);
        String itemName = item == null ? "the item" : item.getName();

        notifications.claimUpdate(c.getClaimantId(), "CLAIM_APPROVED",
                "Your claim for \"" + itemName + "\" was approved. Open it to see where to collect the item.",
                c.getItemId(), c.getId());
        if (item != null)
            notifications.claimUpdate(item.getReporterId(), "ITEM_CLAIMED",
                    "The owner of \"" + itemName + "\" has been found. Thank you for reporting it!",
                    c.getItemId(), null);

        DropOrClaimed place = (item == null || item.getDropLocationId() == null) ? null
                : dropLocations.findById(item.getDropLocationId()).orElse(null);
        emailClaimant(c, ClaimEmailEvent.Kind.APPROVED, itemName, c.getReviewNote(), place, now);

        matching.settleAfterApproval(c.getItemId(), c.getClaimantId());

        for (Claim other : claims.findByItemIdAndStatus(c.getItemId(), ClaimStatus.PENDING)) {
            if (!other.getId().equals(c.getId())) {
                finish(other, ClaimStatus.REJECTED, staffId, "Another claim for this item was approved", now);
                notifications.claimUpdate(other.getClaimantId(), "CLAIM_REJECTED",
                        "Your claim for \"" + itemName + "\" was not approved: another claim for this item was approved.",
                        other.getItemId(), other.getId());
                emailClaimant(other, ClaimEmailEvent.Kind.REJECTED, itemName, other.getReviewNote(), null, now);
            }
        }
        return toStaffResponses(List.of(c)).get(0);
    }


    /** Rejects a claim. A reason is required because the student sees it. */
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
        emailClaimant(c, ClaimEmailEvent.Kind.REJECTED, itemName, reason, null, c.getReviewedAt());
        return toStaffResponses(List.of(c)).get(0);
    }

    // ---- helpers

    /**
     * Publishes an email event. EmailService only sends it AFTER the transaction commits, so a
     * rolled-back approval never sends an email.
     */
    private void emailClaimant(Claim c, ClaimEmailEvent.Kind kind, String itemName,
                               String note, DropOrClaimed place, Instant at) {
        users.findById(c.getClaimantId()).ifPresent(u -> events.publishEvent(new ClaimEmailEvent(
                kind, u.getEmail(), u.getFirstName(), itemName, c.getId(), at, note,
                place == null ? null : place.getName(),
                place == null ? null : place.getContact(),
                place == null ? null : place.getPersonName())));
    }


    /** 404 if missing, 409 if already reviewed, 403 if staff try to review their own claim. */
    private Claim loadPendingClaim(String claimId, String staffId) {
        Claim c = claims.findById(claimId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Claim not found"));
        if (c.getStatus() != ClaimStatus.PENDING)
            throw new ApiException(HttpStatus.CONFLICT, "This claim was already reviewed");
        if (staffId.equals(c.getClaimantId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot review your own claim");
        return c;
    }

    /** Records the decision: who decided, when, and the note. */
    private void finish(Claim c, ClaimStatus status, String staffId, String note, Instant at) {
        c.setStatus(status);
        c.setReviewedBy(staffId);
        c.setReviewNote(note);
        c.setReviewedAt(at);
        claims.save(c);
    }

    /** Blank notes become null; otherwise trimmed. */
    private static String clean(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /**
     * Builds the staff view in a few bulk lookups (users, items, drop-off places) instead of one per claim.
     */
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