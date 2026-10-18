package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.CategoryRequest;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Categories;
import com.lostandfound.backend.repository.CategoryRepository;
import com.lostandfound.backend.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categories;
    private final ItemRepository items;

    public CategoryService(CategoryRepository categories, ItemRepository items) {
        this.categories = categories;
        this.items = items;
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
        if (items.existsByCategoryId(id))
            throw new ApiException(HttpStatus.CONFLICT, "Category is in use by existing items");
        categories.deleteById(id);
    }
}