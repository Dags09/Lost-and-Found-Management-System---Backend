package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.DropLocationRequest;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.DropOrClaimed;
import com.lostandfound.backend.repository.DropLocationRepository;
import com.lostandfound.backend.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DropLocationService {

    private final DropLocationRepository locations;
    private final ItemRepository items;

    public DropLocationService(DropLocationRepository locations, ItemRepository items) {
        this.locations = locations;
        this.items = items;
    }

    public List<DropOrClaimed> list() {
        return locations.findAll();
    }

    public DropOrClaimed create(DropLocationRequest req) {
        String name = req.name().trim();
        if (locations.existsByNameIgnoreCase(name))
            throw new ApiException(HttpStatus.CONFLICT, "Drop-off location already exists");
        DropOrClaimed d = new DropOrClaimed();
        d.setName(name);
        d.setContact(req.contact().trim());
        return locations.save(d);
    }

    public DropOrClaimed update(String id, DropLocationRequest req) {
        DropOrClaimed d = locations.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Drop-off location not found"));
        String name = req.name().trim();
        if (!d.getName().equalsIgnoreCase(name) && locations.existsByNameIgnoreCase(name))
            throw new ApiException(HttpStatus.CONFLICT, "Drop-off location already exists");
        d.setName(name);
        d.setContact(req.contact().trim());
        return locations.save(d);
    }

    public void delete(String id) {
        if (!locations.existsById(id))
            throw new ApiException(HttpStatus.NOT_FOUND, "Drop-off location not found");
        if (items.existsByDropLocationId(id))
            throw new ApiException(HttpStatus.CONFLICT, "Drop-off location is in use by existing items");
        locations.deleteById(id);
    }
}