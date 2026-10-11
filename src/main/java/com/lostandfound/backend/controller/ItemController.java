package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.ItemCreateRequest;
import com.lostandfound.backend.dto.ItemPublicResponse;
import com.lostandfound.backend.dto.MyItemResponse;
import com.lostandfound.backend.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemPublicResponse create(Authentication authentication,
                                     @Valid @RequestBody ItemCreateRequest req) {
        return service.create((String) authentication.getPrincipal(), req);
    }

    @GetMapping
    public List<ItemPublicResponse> list() {
        return service.listOpen();
    }

    @GetMapping("/mine")
    public List<MyItemResponse> mine(Authentication authentication) {
        return service.listMine((String) authentication.getPrincipal());
    }

    @GetMapping("/{id}")
    public ItemPublicResponse get(@PathVariable String id) {
        return service.getPublic(id);
    }
}