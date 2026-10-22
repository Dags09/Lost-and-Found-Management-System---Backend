package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.CategoryRequest;
import com.lostandfound.backend.models.Categories;
import com.lostandfound.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** /v1/categories: anyone can read; only Admin can change. */
@RestController
@RequestMapping("/v1/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    /** GET /v1/categories : public, used for the dropdowns in the report forms. */
    @GetMapping
    public List<Categories> list() {
        return service.list();
    }

    /** POST /v1/categories : Admin only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Admin')")
    public Categories create(@Valid @RequestBody CategoryRequest req) {
        return service.create(req);
    }

    /** PUT /v1/categories/{id} : Admin only. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public Categories update(@PathVariable String id, @Valid @RequestBody CategoryRequest req) {
        return service.update(id, req);
    }

    /** DELETE /v1/categories/{id} : Admin only. 409 if the category is in use. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Admin')")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}