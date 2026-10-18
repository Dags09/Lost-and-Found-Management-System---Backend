package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.ClaimResponse;
import com.lostandfound.backend.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class ClaimController {

    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    @PostMapping(value = "/items/{itemId}/claims", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse submit(@PathVariable String itemId,
                                Authentication authentication,
                                @RequestParam("proofDescription") String proofDescription,
                                @RequestParam("proofImage") MultipartFile proofImage) {
        return service.submit(itemId, (String) authentication.getPrincipal(), proofDescription, proofImage);
    }

    @GetMapping("/claims/mine")
    public List<ClaimResponse> mine(Authentication authentication) {
        return service.listMine((String) authentication.getPrincipal());
    }
}