package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.LostItemRequest;
import com.lostandfound.backend.dto.MatchResponse;
import com.lostandfound.backend.dto.MyItemResponse;
import com.lostandfound.backend.service.LostItemService;
import com.lostandfound.backend.service.MatchService;
import jakarta.validation.Valid;
import com.lostandfound.backend.models.ImageFile;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/lost-items")
public class LostItemController {

    private final LostItemService service;
    private final MatchService matching;

    public LostItemController(LostItemService service, MatchService matching) {
        this.service = service;
        this.matching = matching;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MyItemResponse create(Authentication authentication, @Valid @RequestBody LostItemRequest req) {
        return service.create((String) authentication.getPrincipal(), req);
    }

    @GetMapping("/mine")
    public List<MyItemResponse> mine(Authentication authentication) {
        return service.listMine((String) authentication.getPrincipal());
    }

    @PutMapping("/{id}")
    public MyItemResponse update(@PathVariable String id, Authentication authentication,
                                 @Valid @RequestBody LostItemRequest req) {
        return service.update(id, (String) authentication.getPrincipal(), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, Authentication authentication) {
        service.delete(id, (String) authentication.getPrincipal());
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MyItemResponse addImage(@PathVariable String id, Authentication authentication,
                                   @RequestParam("file") MultipartFile file) {
        return service.addImage(id, (String) authentication.getPrincipal(), file);
    }

    @DeleteMapping("/{id}/images/{position}")
    public MyItemResponse removeImage(@PathVariable String id, @PathVariable int position,
                                      Authentication authentication) {
        return service.removeImage(id, (String) authentication.getPrincipal(), position);
    }

    @GetMapping("/{id}/images/{position}")
    public ResponseEntity<byte[]> image(@PathVariable String id, @PathVariable int position,
                                        Authentication authentication) {
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Moderator") || a.getAuthority().equals("ROLE_Admin"));
        ImageFile img = service.getImage(id, position, (String) authentication.getPrincipal(), staff);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.getContentType()))
                .contentLength(img.getSizeBytes())
                .cacheControl(CacheControl.noStore())
                .body(img.getData());
    }

    @GetMapping("/{id}/matches")
    public List<MatchResponse> matches(@PathVariable String id, Authentication authentication) {
        return matching.listForLost(id, (String) authentication.getPrincipal());
    }

    @PostMapping("/{id}/matches/{matchId}/dismiss")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dismiss(@PathVariable String id, @PathVariable String matchId, Authentication authentication) {
        matching.dismiss(id, matchId, (String) authentication.getPrincipal());
    }

    @PostMapping("/{id}/close")
    public MyItemResponse close(@PathVariable String id, Authentication authentication) {
        return service.close(id, (String) authentication.getPrincipal());
    }
}