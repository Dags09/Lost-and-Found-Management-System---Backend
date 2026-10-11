package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.CategoryRequest;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Categories;
import com.lostandfound.backend.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    public List<Categories> list() {
        return categories.findAll();
    }

    public Categories create(CategoryRequest req) {
        String name = req.name().trim();
        if (categories.existsByNameIgnoreCase(name))
            throw new ApiException(HttpStatus.CONFLICT, "Category already exists");
        Categories c = new Categories();
        c.setName(name);
        c.setIcon(req.icon().trim());
        return categories.save(c);
    }

    public Categories update(String id, CategoryRequest req) {
        Categories c = categories.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        String name = req.name().trim();
        if (!c.getName().equalsIgnoreCase(name) && categories.existsByNameIgnoreCase(name))
            throw new ApiException(HttpStatus.CONFLICT, "Category already exists");
        c.setName(name);
        c.setIcon(req.icon().trim());
        return categories.save(c);
    }

    public void delete(String id) {
        if (!categories.existsById(id))
            throw new ApiException(HttpStatus.NOT_FOUND, "Category not found");
        categories.deleteById(id);
    }
}