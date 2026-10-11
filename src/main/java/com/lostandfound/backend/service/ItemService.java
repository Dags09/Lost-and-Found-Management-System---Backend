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

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository items;
    private final CategoryRepository categories;
    private final DropLocationRepository dropLocations;

    public ItemService(ItemRepository items, CategoryRepository categories,
                       DropLocationRepository dropLocations) {
        this.items = items;
        this.categories = categories;
        this.dropLocations = dropLocations;
    }

    public ItemPublicResponse create(String reporterId, ItemCreateRequest req) {
        if (!categories.existsById(req.categoryId()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category does not exist");
        if (!dropLocations.existsById(req.dropLocationId()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Drop-off location does not exist");

        Items i = new Items();
        i.setReporterId(reporterId);
        i.setType(ItemType.FOUND);            // Option A: every report is a found item
        i.setName(req.name().trim());
        i.setDescription(req.description().trim());
        i.setCategoryId(req.categoryId());
        i.setColor(req.color().trim());
        i.setBrand(req.brand().trim());
        i.setDistinguishingMarks(req.distinguishingMarks().trim());
        i.setLocationName(req.locationName().trim());
        i.setDropLocationId(req.dropLocationId());
        i.setDateOccurred(req.dateOccurred());
        return ItemPublicResponse.from(items.save(i));
    }

    public List<ItemPublicResponse> listOpen() {
        return items.findByStatusOrderByCreatedAtDesc(ItemStatus.OPEN)
                .stream()
                .map(ItemPublicResponse::from)
                .toList();
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
}