package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.ItemCreateRequest;
import com.lostandfound.backend.dto.ItemPublicResponse;
import com.lostandfound.backend.dto.MyItemResponse;
import com.lostandfound.backend.dto.PageResponse;
import com.lostandfound.backend.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public PageResponse<ItemPublicResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(q, categoryId, page, size);
    }

    @GetMapping("/mine")
    public List<MyItemResponse> mine(Authentication authentication) {
        return service.listMine((String) authentication.getPrincipal());
    }

    @GetMapping("/{id}")
    public ItemPublicResponse get(@PathVariable String id) {
        return service.getPublic(id);
    }

    @PutMapping("/{id}")
    public MyItemResponse update(@PathVariable String id,
                                 Authentication authentication,
                                 @Valid @RequestBody ItemCreateRequest req) {
        return service.update(id, (String) authentication.getPrincipal(), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, Authentication authentication) {
        service.delete(id, (String) authentication.getPrincipal());
    }
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MyItemResponse addImage(@PathVariable String id,
                                   Authentication authentication,
                                   @RequestParam("file") MultipartFile file) {
        return service.addImage(id, (String) authentication.getPrincipal(), file);
    }

    @DeleteMapping("/{id}/images/{position}")
    public MyItemResponse removeImage(@PathVariable String id,
                                      @PathVariable int position,
                                      Authentication authentication) {
        return service.removeImage(id, (String) authentication.getPrincipal(), position);
    }
}