package com.lostandfound.backend.service;



import com.lostandfound.backend.dto.ItemCreateRequest;
import com.lostandfound.backend.dto.ItemPublicResponse;
import com.lostandfound.backend.dto.MyItemResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.ItemImage;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
import com.lostandfound.backend.repository.CategoryRepository;
import com.lostandfound.backend.repository.ClaimRepository;
import com.lostandfound.backend.repository.DropLocationRepository;
import com.lostandfound.backend.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.lostandfound.backend.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private static final int MAX_IMAGES = 3;

    private final ItemRepository items;
    private final CategoryRepository categories;
    private final DropLocationRepository dropLocations;
    private final ImageStorageService imageStorage;
    private final ClaimRepository claims;

    public ItemService(ItemRepository items, CategoryRepository categories,
                       DropLocationRepository dropLocations,
                       ImageStorageService imageStorage, ClaimRepository claims) {
        this.items = items;
        this.categories = categories;
        this.dropLocations = dropLocations;
        this.imageStorage = imageStorage;
        this.claims = claims;
    }

    public ItemPublicResponse create(String reporterId, ItemCreateRequest req) {
        Items i = new Items();
        i.setReporterId(reporterId);
        i.setType(ItemType.FOUND);            // Option A: every report is a found item
        apply(i, req);
        return ItemPublicResponse.from(items.save(i));
    }

    public MyItemResponse update(String id, String userId, ItemCreateRequest req) {
        Items i = findOwnedOpenItem(id, userId);
        apply(i, req);
        return MyItemResponse.from(items.save(i));
    }

    public void delete(String id, String userId) {
        Items i = findOwnedOpenItem(id, userId);
        i.getImages().forEach(img -> imageStorage.delete(img.getPublicId()));
        items.delete(i);
    }

    public PageResponse<ItemPublicResponse> search(String q, String categoryId,
                                                   String dropLocationId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String category = (categoryId == null || categoryId.isBlank()) ? null : categoryId;
        String dropLocation = (dropLocationId == null || dropLocationId.isBlank()) ? null : dropLocationId;
        PageRequest pageable = PageRequest.of(safePage, safeSize);

        // With words: full-text search, best match first. Without: newest first.
        String tsQuery = toTsQuery(q);
        Page<Items> result = tsQuery != null
                ? items.searchOpen(tsQuery, category, dropLocation, pageable)
                : items.browseOpen(category, dropLocation, pageable);

        List<ItemPublicResponse> content = result.getContent().stream()
                .map(ItemPublicResponse::from)
                .toList();
        return new PageResponse<>(content, safePage, safeSize,
                result.getTotalElements(), result.getTotalPages());
    }

    /** "black  wallet!" -> "black | wallet" (match ANY word). Null when nothing searchable is left. */
    private static String toTsQuery(String q) {
        if (q == null || q.isBlank()) return null;
        String joined = Arrays.stream(q.trim().split("[^\\p{L}\\p{N}]+"))
                .filter(w -> !w.isEmpty())
                .collect(Collectors.joining(" | "));
        return joined.isEmpty() ? null : joined;
    }

    public ItemPublicResponse getPublic(String id) {
        return items.findById(id)
                .map(ItemPublicResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    public List<MyItemResponse> listMine(String reporterId) {
        return items.findByReporterIdOrderByCreatedAtDesc(reporterId)
                .stream()
                .map(MyItemResponse::from)
                .toList();
    }

    public MyItemResponse addImage(String id, String userId, MultipartFile file) {
        Items i = findOwnedOpenItem(id, userId);
        if (i.getImages().size() >= MAX_IMAGES)
            throw new ApiException(HttpStatus.CONFLICT, "An item can have at most " + MAX_IMAGES + " images");
        ImageStorageService.StoredImage stored = imageStorage.upload(file);
        i.getImages().add(new ItemImage(stored.url(), stored.publicId(), i.getImages().size()));
        return MyItemResponse.from(items.save(i));
    }

    public MyItemResponse removeImage(String id, String userId, int position) {
        Items i = findOwnedOpenItem(id, userId);
        ItemImage target = i.getImages().stream()
                .filter(img -> img.getPosition() == position)
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Image not found"));
        imageStorage.delete(target.getPublicId());
        i.getImages().remove(target);
        for (int n = 0; n < i.getImages().size(); n++)
            i.getImages().get(n).setPosition(n);          // keep positions 0,1,2 with no gaps
        return MyItemResponse.from(items.save(i));
    }

    // ---- helpers -------------------------------------------------------

    /** Loads an item and enforces: it exists (404), it is yours (403), it is still open (409). */
    private Items findOwnedOpenItem(String id, String userId) {
        Items i = items.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
        if (!userId.equals(i.getReporterId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only change your own reports");
        if (i.getStatus() != ItemStatus.OPEN)
            throw new ApiException(HttpStatus.CONFLICT, "Only open items can be changed");
        if (claims.existsByItemIdAndStatus(id, ClaimStatus.PENDING))
            throw new ApiException(HttpStatus.CONFLICT, "This item has pending claims and cannot be changed");
        return i;
    }

    /** Validates the references and copies the request fields onto the item. */
    private void apply(Items i, ItemCreateRequest req) {
        if (!categories.existsById(req.categoryId()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category does not exist");
        if (!dropLocations.existsById(req.dropLocationId()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Drop-off location does not exist");

        i.setName(req.name().trim());
        i.setDescription(req.description().trim());
        i.setCategoryId(req.categoryId());
        i.setColor(req.color().trim());
        i.setBrand(req.brand().trim());
        i.setDistinguishingMarks(req.distinguishingMarks().trim());
        i.setLocationName(req.locationName().trim());
        i.setDropLocationId(req.dropLocationId());
        i.setDateOccurred(req.dateOccurred());
    }
}