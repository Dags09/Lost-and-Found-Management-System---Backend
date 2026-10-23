package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.ClaimResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.DropOrClaimed;
import com.lostandfound.backend.models.ImageFile;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
import com.lostandfound.backend.repository.ClaimRepository;
import com.lostandfound.backend.repository.DropLocationRepository;
import com.lostandfound.backend.repository.ItemRepository;
import com.lostandfound.backend.mail.ClaimEmailEvent;
import com.lostandfound.backend.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.lostandfound.backend.models.enumRoleStatusTypes.Purpose;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Student side of claims: submit a claim with proof, cancel a pending one, view own claims, load the proof photo. */
@Service
public class ClaimService {

    private static final int MAX_DESCRIPTION = 1000;

    private final ClaimRepository claims;
    private final ItemRepository items;
    private final ImageStorageService imageStorage;
    private final DropLocationRepository dropLocations;
    private final NotificationService notifications;
    private final UserRepository users;
    private final ApplicationEventPublisher events;

    public ClaimService(ClaimRepository claims, ItemRepository items,
                        ImageStorageService imageStorage, DropLocationRepository dropLocations,
                        NotificationService notifications,
                        UserRepository users, ApplicationEventPublisher events) {
        this.users = users;
        this.events = events;
        this.claims = claims;
        this.items = items;
        this.imageStorage = imageStorage;
        this.dropLocations = dropLocations;
        this.notifications = notifications;
    }

    /**
     * Submits a claim for a found item.
     * Rules: the item must be a FOUND item, still OPEN, not reported by the claimant, and the claimant
     * may have only one PENDING claim per item (also enforced by a unique index in schema.sql).
     * The proof photo and the claim are saved in one transaction, so a failure leaves no orphan photo.
     */
    @Transactional
    public ClaimResponse submit(String itemId, String claimantId,
                                String proofDescription, MultipartFile proofImage) {
        // 1. Cheap checks first, before spending an upload
        if (proofDescription == null || proofDescription.isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Describe how you can prove this item is yours");
        if (proofDescription.length() > MAX_DESCRIPTION)
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Description is too long (max " + MAX_DESCRIPTION + " characters)");

        Items item = items.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
        if (item.getType() != ItemType.FOUND)   // you can only claim something someone found
            throw new ApiException(HttpStatus.NOT_FOUND, "Item not found");
        if (item.getStatus() != ItemStatus.OPEN)
            throw new ApiException(HttpStatus.CONFLICT, "This item is no longer open for claims");
        if (claimantId.equals(item.getReporterId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot claim an item you reported");
        if (claims.existsByItemIdAndClaimantIdAndStatus(itemId, claimantId, ClaimStatus.PENDING))
            throw new ApiException(HttpStatus.CONFLICT, "You already have a pending claim for this item");

        // 2. Save the proof photo (into PostgreSQL) and the claim in ONE transaction:
        //    if anything fails, both roll back, so no orphan photo is left behind.
        String imageId = imageStorage.save(proofImage, Purpose.PROOF);

        Claim c = new Claim();
        c.setItemId(itemId);
        c.setClaimantId(claimantId);
        c.setProofDescription(proofDescription.trim());
        c.setProofImageId(imageId);
        Claim saved = claims.saveAndFlush(c);


        notifications.claimUpdate(item.getReporterId(), "CLAIM_SUBMITTED",
                "Someone submitted a claim for \"" + item.getName() + "\". Staff will review it.",
                itemId, null);

        users.findById(claimantId).ifPresent(u -> events.publishEvent(new ClaimEmailEvent(
                ClaimEmailEvent.Kind.SUBMITTED, u.getEmail(), u.getFirstName(), item.getName(),
                saved.getId(), saved.getCreatedAt(), null, null, null, null)));
        return ClaimResponse.from(saved);
    }

    /**
     * The claimant withdraws their own claim while it is still PENDING.
     * Someone else's claim gives 404 (as if it did not exist). An already reviewed or cancelled claim
     * gives 409. The row is locked so staff approving the same claim at the same moment cannot clash.
     * The proof photo is kept, so the claim still shows in "my claims" as CANCELLED. The student may
     * submit a new claim for the item afterwards, because the one-pending-claim rule only counts PENDING.
     */
    @Transactional
    public ClaimResponse cancel(String claimId, String claimantId) {
        Claim c = claims.findByIdForUpdate(claimId)
                .filter(x -> claimantId.equals(x.getClaimantId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Claim not found"));
        if (c.getStatus() != ClaimStatus.PENDING)
            throw new ApiException(HttpStatus.CONFLICT, "Only a pending claim can be cancelled");
        c.setStatus(ClaimStatus.CANCELLED);
        return ClaimResponse.from(claims.save(c));
    }

    /**
     * Only the claimant and staff may load the proof photo. Anyone else gets 404, as if it did not exist.
     */
    @Transactional(readOnly = true)
    public ImageFile getProofImage(String claimId, String userId, boolean isStaff) {
        Claim c = claims.findById(claimId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Image not found"));
        if (!isStaff && !userId.equals(c.getClaimantId()))
            throw new ApiException(HttpStatus.NOT_FOUND, "Image not found");
        if (c.getProofImageId() == null)
            throw new ApiException(HttpStatus.NOT_FOUND, "Image not found");
        return imageStorage.get(c.getProofImageId(), Purpose.PROOF);
    }

    /**
     * The student's own claims, newest first. For APPROVED claims it also loads the drop-off place, so
     * the student knows where to collect the item.
     */
    @Transactional(readOnly = true)
    public List<ClaimResponse> listMine(String claimantId) {
        List<Claim> mine = claims.findByClaimantIdOrderByCreatedAtDesc(claimantId);

        List<String> approvedItemIds = mine.stream()
                .filter(c -> c.getStatus() == ClaimStatus.APPROVED)
                .map(Claim::getItemId)
                .toList();
        Map<String, DropOrClaimed> dropOffByItem = new HashMap<>();
        if (!approvedItemIds.isEmpty()) {
            List<Items> approvedItems = items.findAllById(approvedItemIds);
            Map<String, DropOrClaimed> places = new HashMap<>();
            dropLocations.findAllById(approvedItems.stream()
                            .map(Items::getDropLocationId).filter(Objects::nonNull).distinct().toList())
                    .forEach(d -> places.put(d.getId(), d));
            approvedItems.forEach(i -> {
                DropOrClaimed d = places.get(i.getDropLocationId());
                if (d != null) dropOffByItem.put(i.getId(), d);
            });
        }

        return mine.stream()
                .map(c -> ClaimResponse.from(c, dropOffByItem.get(c.getItemId())))
                .toList();
    }
}