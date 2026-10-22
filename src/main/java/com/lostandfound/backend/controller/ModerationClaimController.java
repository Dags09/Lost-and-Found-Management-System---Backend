package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.ReviewRequest;
import com.lostandfound.backend.dto.StaffClaimResponse;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import com.lostandfound.backend.service.ClaimReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/moderation/claims")
public class ModerationClaimController {

    private final ClaimReviewService service;

    public ModerationClaimController(ClaimReviewService service) {
        this.service = service;
    }

    @GetMapping
    public List<StaffClaimResponse> list(@RequestParam(defaultValue = "PENDING") ClaimStatus status) {
        return service.list(status);
    }

    @PostMapping("/{id}/approve")
    public StaffClaimResponse approve(@PathVariable String id, Authentication authentication,
                                      @Valid @RequestBody(required = false) ReviewRequest body) {
        return service.approve(id, (String) authentication.getPrincipal(), body == null ? null : body.note());
    }

    @PostMapping("/{id}/reject")
    public StaffClaimResponse reject(@PathVariable String id, Authentication authentication,
                                     @Valid @RequestBody(required = false) ReviewRequest body) {
        return service.reject(id, (String) authentication.getPrincipal(), body == null ? null : body.note());
    }
}