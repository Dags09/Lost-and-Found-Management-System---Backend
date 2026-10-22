package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.ReviewRequest;
import com.lostandfound.backend.dto.StaffClaimResponse;
import com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus;
import com.lostandfound.backend.service.ClaimReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/** /v1/moderation/claims: staff review claims (Moderator or Admin). */
@RestController
@RequestMapping("/v1/moderation/claims")
public class ModerationClaimController {

    private final ClaimReviewService service;

    public ModerationClaimController(ClaimReviewService service) {
        this.service = service;
    }

    /** GET /v1/moderation/claims?status=PENDING : claims to review, oldest first. */
    @GetMapping
    public List<StaffClaimResponse> list(@RequestParam(defaultValue = "PENDING") ClaimStatus status) {
        return service.list(status);
    }

    /** POST /v1/moderation/claims/{id}/approve : the note is optional. */
    @PostMapping("/{id}/approve")
    public StaffClaimResponse approve(@PathVariable String id, Authentication authentication,
                                      @Valid @RequestBody(required = false) ReviewRequest body) {
        return service.approve(id, (String) authentication.getPrincipal(), body == null ? null : body.note());
    }

    /** POST /v1/moderation/claims/{id}/reject : the note (reason) is required. */
    @PostMapping("/{id}/reject")
    public StaffClaimResponse reject(@PathVariable String id, Authentication authentication,
                                     @Valid @RequestBody(required = false) ReviewRequest body) {
        return service.reject(id, (String) authentication.getPrincipal(), body == null ? null : body.note());
    }
}