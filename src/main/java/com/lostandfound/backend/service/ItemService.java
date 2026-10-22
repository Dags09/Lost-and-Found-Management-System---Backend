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
import com.lostandfound.backend.models.enumRoleStatusTypes.Purpose;
import com.lostandfound.backend.repository.CategoryRepository;
import com.lostandfound.backend.repository.ClaimRepository;
import com.lostandfound.backend.repository.DropLocationRepository;
import com.lostandfound.backend.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lostandfound.backend.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private static final int MAX_IMAGES = 3;

    private final ItemRepository items;
    private final CategoryRepository categories;
    private final DropLocationRepository dropLocations;
    private final ImageStorageService imageStorage;
    private final ClaimRepository claims;
    private final MatchService matching;

    public ItemService(ItemRepository items, CategoryRepository categories,
                       DropLocationRepository dropLocations,
                       ImageStorageService imageStorage, ClaimRepository claims,
                       MatchService matching) {
        this.items = items;
        this.categories = categories;
        this.dropLocations = dropLocations;
        this.imageStorage = imageStorage;
        this.claims = claims;
        this.matching = matching;
    }

    public ItemPublicResponse create(String reporterId, ItemCreateRequest req) {
        Items i = new Items();
        i.setReporterId(reporterId);
        i.setType(ItemType.FOUND);            // Option A: every report is a found item
        apply(i, req);
        Items saved = items.save(i);
        findMatches(saved);
        return ItemPublicResponse.from(saved);
    }

    public MyItemResponse update(String id, String userId, ItemCreateRequest req) {
        Items i = findOwnedOpenItem(id, userId);
        apply(i, req);
        Items saved = items.save(i);
        findMatches(saved);                  // an edit may create new matches; existing ones are kept
        return MyItemResponse.from(saved);
    }

    @Transactional
    public void delete(String id, String userId) {
        Items i = findOwnedOpenItem(id, userId);

        // Collect every picture that belongs to this item (its photos + proof photos of old claims)
        List<String> imageIds = new ArrayList<>();
        i.getImages().forEach(img -> imageIds.add(img.getImageId()));
        claims.findByItemId(id).forEach(c -> imageIds.add(c.getProofImageId()));
        imageIds.removeIf(java.util.Objects::isNull);

        items.delete(i);
        items.flush();                       // the database cascades the claims/item_images rows away
        imageStorage.deleteAll(imageIds);    // now nothing points at the images any more
    }

    public PageResponse<ItemPublicResponse> search(String q, String categoryId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String category = (categoryId == null || categoryId.isBlank()) ? null : categoryId;
        PageRequest pageable = PageRequest.of(safePage, safeSize);

        // With words: prefix + fuzzy search, best match first. Without: newest first.
        List<String> words = words(q);
        String tsQuery = toTsQuery(words);
        String phrase = String.join(" ", words);

        Page<Items> result = tsQuery != null
                ? items.searchOpen(tsQuery, phrase, category, pageable)
                : items.browseOpen(category, pageable);

        List<ItemPublicResponse> content = result.getContent().stream()
                .map(ItemPublicResponse::from)
                .toList();
        return new PageResponse<>(content, safePage, safeSize,
                result.getTotalElements(), result.getTotalPages());
    }

    /** "black  wallet!" -> "black | wallet" (match ANY word). Null when nothing searchable is left. */
    private static final int MAX_WORDS = 8;

    private static List<String> words(String q) {
        if (q == null || q.isBlank()) return List.of();
        return Arrays.stream(q.trim().toLowerCase().split("[^\\p{L}\\p{N}]+"))
                .filter(w -> !w.isEmpty())
                .limit(MAX_WORDS)
                .toList();
    }

    /** "black wall" -> "black:* | wall:*"  (each word matches as a prefix, ANY word counts). */
    private static String toTsQuery(List<String> words) {
        return words.isEmpty() ? null
                : words.stream().map(w -> w + ":*").collect(Collectors.joining(" | "));
    }

    public ItemPublicResponse getPublic(String id) {
        return items.findById(id)
                .filter(i -> i.getType() == ItemType.FOUND)   // lost reports are private
                .map(ItemPublicResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    public List<MyItemResponse> listMine(String reporterId) {
        return items.findByReporterIdAndTypeOrderByCreatedAtDesc(reporterId, ItemType.FOUND)
                .stream()
                .map(MyItemResponse::from)
                .toList();
    }

    @Transactional
    public MyItemResponse addImage(String id, String userId, MultipartFile file) {
        Items i = findOwnedOpenItem(id, userId);
        if (i.getImages().size() >= MAX_IMAGES)
            throw new ApiException(HttpStatus.CONFLICT, "An item can have at most " + MAX_IMAGES + " images");
        String imageId = imageStorage.save(file, Purpose.ITEM);
        i.getImages().add(new ItemImage(imageId, i.getImages().size()));
        return MyItemResponse.from(items.save(i));
    }

    @Transactional
    public MyItemResponse removeImage(String id, String userId, int position) {
        Items i = findOwnedOpenItem(id, userId);
        ItemImage target = i.getImages().stream()
                .filter(img -> img.getPosition() == position)
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Image not found"));
        i.getImages().remove(target);
        for (int n = 0; n < i.getImages().size(); n++)
            i.getImages().get(n).setPosition(n);
        Items saved = items.saveAndFlush(i);
        imageStorage.delete(target.getImageId());
        return MyItemResponse.from(saved);
    }

    // ---- helpers -------------------------------------------------------

    /** Matching is a bonus: if it fails, the report itself must still succeed. */
    private void findMatches(Items saved) {
        try {
            matching.matchNewFound(saved);
        } catch (RuntimeException e) {
            log.warn("Matching failed for found item {}", saved.getId(), e);
        }
    }

    /** Loads an item and enforces: it exists (404), it is yours (403), it is still open (409). */
    private Items findOwnedOpenItem(String id, String userId) {
        Items i = items.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
        if (i.getType() != ItemType.FOUND)   // lost reports have their own endpoints
            throw new ApiException(HttpStatus.NOT_FOUND, "Item not found");
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