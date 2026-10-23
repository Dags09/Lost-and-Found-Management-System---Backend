package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.LostItemRequest;
import com.lostandfound.backend.dto.MyItemResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.ImageFile;
import com.lostandfound.backend.models.ItemImage;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
import com.lostandfound.backend.models.enumRoleStatusTypes.Purpose;
import com.lostandfound.backend.repository.CategoryRepository;
import com.lostandfound.backend.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;


/**
 * Lost reports ("I lost something"). Private to the reporter and staff. Creating or editing a
 * report triggers matching against the open found items.
 */
@Service
public class LostItemService {

    private static final Logger log = LoggerFactory.getLogger(LostItemService.class);

    private final ItemRepository items;
    private final CategoryRepository categories;

    private static final int MAX_IMAGES = 3;

    private final ImageStorageService imageStorage;
    private final MatchService matching;

    public LostItemService(ItemRepository items, CategoryRepository categories,
                           ImageStorageService imageStorage, MatchService matching) {
        this.items = items;
        this.categories = categories;
        this.imageStorage = imageStorage;
        this.matching = matching;
    }


    /** Creates a lost report. type is set here, never taken from the client. */
    public MyItemResponse create(String reporterId, LostItemRequest req) {
        Items i = new Items();
        i.setReporterId(reporterId);
        i.setType(ItemType.LOST);            
        apply(i, req);
        Items saved = items.save(i);
        findMatches(saved);
        return MyItemResponse.from(saved);
    }

    /** Edits your own OPEN lost report. */
    public MyItemResponse update(String id, String userId, LostItemRequest req) {
        Items i = findOwnedOpenLost(id, userId);
        apply(i, req);
        Items saved = items.save(i);
        findMatches(saved);                  // an edit may create new matches; existing ones are kept
        return MyItemResponse.from(saved);
    }

    /** Deletes your own OPEN lost report and its photos. */
    @Transactional
    public void delete(String id, String userId) {
        Items i = findOwnedOpenLost(id, userId);
        List<String> imageIds = i.getImages().stream().map(ItemImage::getImageId)
                .filter(Objects::nonNull).toList();
        items.delete(i);
        items.flush();                       // the database cascades the photo rows away
        imageStorage.deleteAll(imageIds);    // now nothing points at the pictures any more
    }

    /** Adds a photo (max 3). Lost-report photos are private: only the owner and staff can load them. */
    @Transactional
    public MyItemResponse addImage(String id, String userId, MultipartFile file) {
        Items i = findOwnedOpenLost(id, userId);
        if (i.getImages().size() >= MAX_IMAGES)
            throw new ApiException(HttpStatus.CONFLICT, "A report can have at most " + MAX_IMAGES + " photos");
        String imageId = imageStorage.save(file, Purpose.LOST_ITEM);
        i.getImages().add(new ItemImage(imageId, i.getImages().size()));
        return MyItemResponse.from(items.save(i));
    }

    /** Removes the photo at that position and renumbers the rest. */
    @Transactional
    public MyItemResponse removeImage(String id, String userId, int position) {
        Items i = findOwnedOpenLost(id, userId);
        ItemImage target = i.getImages().stream()
                .filter(img -> img.getPosition() == position)
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Photo not found"));
        i.getImages().remove(target);
        for (int n = 0; n < i.getImages().size(); n++)
            i.getImages().get(n).setPosition(n);
        Items saved = items.saveAndFlush(i);
        imageStorage.delete(target.getImageId());
        return MyItemResponse.from(saved);
    }

    /** Only the owner and staff may load a lost-report photo. Anyone else gets 404, as if it did not exist. */
    @Transactional(readOnly = true)
    public ImageFile getImage(String id, int position, String userId, boolean isStaff) {
        Items i = items.findById(id)
                .filter(x -> x.getType() == ItemType.LOST)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Photo not found"));
        if (!isStaff && !userId.equals(i.getReporterId()))
            throw new ApiException(HttpStatus.NOT_FOUND, "Photo not found");
        String imageId = i.getImages().stream()
                .filter(img -> img.getPosition() == position)
                .map(ItemImage::getImageId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Photo not found"));
        return imageStorage.get(imageId, Purpose.LOST_ITEM);
    }

    /** "I found it myself": closes the report so it stops being matched. */
    @Transactional
    public MyItemResponse close(String id, String userId) {
        Items i = findOwnedOpenLost(id, userId);
        i.setStatus(ItemStatus.CLOSED);
        return MyItemResponse.from(items.save(i));
    }

    /** The caller's own lost reports, newest first. */
    @Transactional(readOnly = true)
    public List<MyItemResponse> listMine(String reporterId) {
        return items.findByReporterIdAndTypeOrderByCreatedAtDesc(reporterId, ItemType.LOST)
                .stream()
                .map(MyItemResponse::from)
                .toList();
    }

    // ---- helpers -------------------------------------------------------

    /** Matching is a bonus: if it fails, the report itself must still succeed. */
    private void findMatches(Items saved) {
        try {
            matching.matchNewLost(saved);
        } catch (RuntimeException e) {
            log.warn("Matching failed for lost report {}", saved.getId(), e);
        }
    }

    /** 404 missing (or not a lost report), 403 not yours, 409 not open any more (OPEN and MATCHED both count as open). */
    private Items findOwnedOpenLost(String id, String userId) {
        Items i = items.findById(id)
                .filter(x -> x.getType() == ItemType.LOST)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lost report not found"));
        if (!userId.equals(i.getReporterId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only change your own reports");
        if (i.getStatus() != ItemStatus.OPEN && i.getStatus() != ItemStatus.MATCHED)
            throw new ApiException(HttpStatus.CONFLICT, "Only open reports can be changed");
        return i;
    }

    /**
     * Copies the request onto the report after checking the category exists. A lost report has no
     * drop-off point (you do not have the item), so dropLocationId is always null.
     */
    private void apply(Items i, LostItemRequest req) {
        if (!categories.existsById(req.categoryId()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category does not exist");
        i.setName(req.name().trim());
        i.setDescription(req.description().trim());
        i.setCategoryId(req.categoryId());
        i.setColor(req.color().trim());
        i.setBrand(req.brand().trim());
        i.setDistinguishingMarks(req.distinguishingMarks().trim());
        i.setLocationName(req.locationName().trim());
        i.setDropLocationId(null);
        i.setDateOccurred(req.dateOccurred());
    }
}