package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.DropLocationOption;
import com.lostandfound.backend.dto.DropLocationRequest;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.DropOrClaimed;
import com.lostandfound.backend.repository.DropLocationRepository;
import com.lostandfound.backend.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/** Drop-off location rules: names are unique ignoring case, and a location in use cannot be deleted. */
@Service
public class DropLocationService {

    private final DropLocationRepository locations;
    private final ItemRepository items;

    public DropLocationService(DropLocationRepository locations, ItemRepository items) {
        this.locations = locations;
        this.items = items;
    }

    /** Full details (place, person, contact): Admin screens only. */
    public List<DropOrClaimed> listDetails() {
        return locations.findAll();
    }

    /** Place names only: safe to show to every logged-in student. */
    public List<DropLocationOption> listOptions() {
        return locations.findAll().stream().map(DropLocationOption::from).toList();
    }

    /** Names are unique ignoring case. */
    public DropOrClaimed create(DropLocationRequest req) {
        String name = req.name().trim();
        if (locations.existsByNameIgnoreCase(name))
            throw new ApiException(HttpStatus.CONFLICT, "Drop-off location already exists");
        DropOrClaimed d = new DropOrClaimed();
        d.setName(name);
        d.setPersonName(req.personName().trim());
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
        d.setPersonName(req.personName().trim());
        d.setContact(req.contact().trim());
        return locations.save(d);
    }

    /** Refused with 409 while any item still uses the location. */
    public void delete(String id) {
        if (!locations.existsById(id))
            throw new ApiException(HttpStatus.NOT_FOUND, "Drop-off location not found");
        if (items.existsByDropLocationId(id))
            throw new ApiException(HttpStatus.CONFLICT, "Drop-off location is in use by existing items");
        locations.deleteById(id);
    }
}