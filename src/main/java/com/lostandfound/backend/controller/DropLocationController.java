package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.DropLocationOption;
import com.lostandfound.backend.dto.DropLocationRequest;
import com.lostandfound.backend.models.DropOrClaimed;
import com.lostandfound.backend.service.DropLocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** /v1/drop-locations: logged-in users see names only; Admin manages the full details. */
@RestController
@RequestMapping("/v1/drop-locations")
public class DropLocationController {

    private final DropLocationService service;

    public DropLocationController(DropLocationService service) {
        this.service = service;
    }


    /** GET /v1/drop-locations : names only, for the "where did you leave it" dropdown. */
    @GetMapping
    public List<DropLocationOption> list() {
        return service.listOptions();
    }


    /** GET /v1/drop-locations/details : includes the contact person and number. Admin only. */
    @GetMapping("/details")
    @PreAuthorize("hasRole('Admin')")
    public List<DropOrClaimed> details() {
        return service.listDetails();
    }

    /** POST /v1/drop-locations : Admin only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Admin')")
    public DropOrClaimed create(@Valid @RequestBody DropLocationRequest req) {
        return service.create(req);
    }

    /** PUT /v1/drop-locations/{id} : Admin only. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public DropOrClaimed update(@PathVariable String id, @Valid @RequestBody DropLocationRequest req) {
        return service.update(id, req);
    }

    /** DELETE /v1/drop-locations/{id} : Admin only. 409 if items use it. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Admin')")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}