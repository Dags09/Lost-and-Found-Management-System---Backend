package com.lostandfound.backend.service;



import com.lostandfound.backend.dto.ItemCreateRequest;
import com.lostandfound.backend.dto.ItemPublicResponse;
import com.lostandfound.backend.dto.MyItemResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
import com.lostandfound.backend.repository.CategoryRepository;
import com.lostandfound.backend.repository.DropLocationRepository;
import com.lostandfound.backend.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.lostandfound.backend.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import java.util.ArrayList;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository items;
    private final CategoryRepository categories;
    private final DropLocationRepository dropLocations;
    private final MongoTemplate mongo;

    public ItemService(ItemRepository items, CategoryRepository categories,
                       DropLocationRepository dropLocations, MongoTemplate mongo) {
        this.items = items;
        this.categories = categories;
        this.dropLocations = dropLocations;
        this.mongo = mongo;
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
        items.delete(i);
    }

    public PageResponse<ItemPublicResponse> search(String q, String categoryId,
                                                   String dropLocationId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        // 1. Build the filters, only adding the ones the client sent
        List<Criteria> filters = new ArrayList<>();
        filters.add(Criteria.where("status").is(ItemStatus.OPEN));
        if (categoryId != null && !categoryId.isBlank())
            filters.add(Criteria.where("categoryId").is(categoryId));
        if (dropLocationId != null && !dropLocationId.isBlank())
            filters.add(Criteria.where("dropLocationId").is(dropLocationId));
        Criteria criteria = new Criteria().andOperator(filters);

        // 2. With words: text search, best match first. Without: newest first.
        Query query;
        if (q != null && !q.isBlank()) {
            TextCriteria text = TextCriteria.forDefaultLanguage()
                    .matchingAny(q.trim().split("\\s+"));
            query = TextQuery.queryText(text).sortByScore().addCriteria(criteria);
        } else {
            query = new Query(criteria).with(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // 3. Count everything that matches, then fetch just one page
        long total = mongo.count(query, Items.class);
        query.with(PageRequest.of(safePage, safeSize));
        List<ItemPublicResponse> content = mongo.find(query, Items.class)
                .stream()
                .map(ItemPublicResponse::from)
                .toList();

        int totalPages = (int) Math.ceil((double) total / safeSize);
        return new PageResponse<>(content, safePage, safeSize, total, totalPages);
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

    // ---- helpers -------------------------------------------------------

    /** Loads an item and enforces: it exists (404), it is yours (403), it is still open (409). */
    private Items findOwnedOpenItem(String id, String userId) {
        Items i = items.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
        if (!userId.equals(i.getReporterId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only change your own reports");
        if (i.getStatus() != ItemStatus.OPEN)
            throw new ApiException(HttpStatus.CONFLICT, "Only open items can be changed");
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