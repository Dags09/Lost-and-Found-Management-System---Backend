package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.ClaimResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Claim;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.repository.ClaimRepository;
import com.lostandfound.backend.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ClaimService {

    private static final int MAX_DESCRIPTION = 1000;

    private final ClaimRepository claims;
    private final ItemRepository items;
    private final ImageStorageService imageStorage;

    public ClaimService(ClaimRepository claims, ItemRepository items, ImageStorageService imageStorage) {
        this.claims = claims;
        this.items = items;
        this.imageStorage = imageStorage;
    }

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
        if (item.getStatus() != ItemStatus.OPEN)
            throw new ApiException(HttpStatus.CONFLICT, "This item is no longer open for claims");
        if (claimantId.equals(item.getReporterId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot claim an item you reported");
        if (claims.existsByItemIdAndClaimantIdAndStatus(itemId, claimantId, ClaimStatus.PENDING))
            throw new ApiException(HttpStatus.CONFLICT, "You already have a pending claim for this item");

        // 2. Upload the proof photo, then save the claim
        ImageStorageService.StoredImage stored = imageStorage.upload(proofImage, "lostandfound/claims");

        Claim c = new Claim();
        c.setItemId(itemId);
        c.setClaimantId(claimantId);
        c.setProofDescription(proofDescription.trim());
        c.setProofImageUrl(stored.url());
        c.setProofImagePublicId(stored.publicId());
        try {
            return ClaimResponse.from(claims.save(c));
        } catch (RuntimeException e) {
            imageStorage.delete(stored.publicId());   // don't leave an orphan photo behind
            throw e;
        }
    }

    public List<ClaimResponse> listMine(String claimantId) {
        return claims.findByClaimantIdOrderByCreatedAtDesc(claimantId)
                .stream()
                .map(ClaimResponse::from)
                .toList();
    }
}