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

@RestController
@RequestMapping("/v1/drop-locations")
public class DropLocationController {

    private final DropLocationService service;

    public DropLocationController(DropLocationService service) {
        this.service = service;
    }


    @GetMapping
    public List<DropLocationOption> list() {
        return service.listOptions();
    }


    @GetMapping("/details")
    @PreAuthorize("hasRole('Admin')")
    public List<DropOrClaimed> details() {
        return service.listDetails();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Admin')")
    public DropOrClaimed create(@Valid @RequestBody DropLocationRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public DropOrClaimed update(@PathVariable String id, @Valid @RequestBody DropLocationRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Admin')")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}