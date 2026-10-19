package com.lostandfound.backend.controller;

import com.lostandfound.backend.models.ImageFile;
import com.lostandfound.backend.models.ImageFile.Purpose;
import com.lostandfound.backend.service.ImageStorageService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/** Serves item photos straight from the database. Claim proofs are NOT served here. */
@RestController
@RequestMapping("/v1/images")
public class ImageController {

    private final ImageStorageService storage;

    public ImageController(ImageStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@PathVariable String id) {
        ImageFile img = storage.get(id, Purpose.ITEM);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.getContentType()))
                .contentLength(img.getSizeBytes())
                // an image id never changes its bytes, so browsers/apps can cache it for a long time
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .body(img.getData());
    }
}