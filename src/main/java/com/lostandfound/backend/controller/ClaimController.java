package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.ClaimResponse;
import com.lostandfound.backend.service.ClaimService;
import com.lostandfound.backend.models.ImageFile;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Student endpoints for claims: submit one for an item, list your own, load a proof photo. */
@RestController
@RequestMapping("/v1")
public class ClaimController {

    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    /** POST /v1/items/{itemId}/claims (multipart) : proofDescription + proofImage are both required. */
    @PostMapping(value = "/items/{itemId}/claims", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse submit(@PathVariable String itemId,
                                Authentication authentication,
                                @RequestParam("proofDescription") String proofDescription,
                                @RequestParam("proofImage") MultipartFile proofImage) {
        return service.submit(itemId, (String) authentication.getPrincipal(), proofDescription, proofImage);
    }

    /** GET /v1/claims/mine : the logged-in student's own claims, newest first. */
    @GetMapping("/claims/mine")
    public List<ClaimResponse> mine(Authentication authentication) {
        return service.listMine((String) authentication.getPrincipal());
    }

    /**
     * GET /v1/claims/{claimId}/proof : the proof photo. Only the claimant and staff; not cached (noStore).
     */
    @GetMapping("/claims/{claimId}/proof")
    public ResponseEntity<byte[]> proof(@PathVariable String claimId, Authentication authentication) {
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Moderator") || a.getAuthority().equals("ROLE_Admin"));
        ImageFile img = service.getProofImage(claimId, (String) authentication.getPrincipal(), staff);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.getContentType()))
                .contentLength(img.getSizeBytes())
                .cacheControl(CacheControl.noStore())
                .body(img.getData());
    }
}