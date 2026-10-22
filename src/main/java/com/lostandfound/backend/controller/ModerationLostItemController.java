package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.PageResponse;
import com.lostandfound.backend.dto.StaffLostItemResponse;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.service.StaffLostItemService;
import org.springframework.web.bind.annotation.*;

/** Staff only: /v1/moderation/** is limited to Moderator and Admin in SecurityConfig. */
@RestController
@RequestMapping("/v1/moderation/lost-items")
public class ModerationLostItemController {

    private final StaffLostItemService service;

    public ModerationLostItemController(StaffLostItemService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<StaffLostItemResponse> list(@RequestParam(defaultValue = "OPEN") ItemStatus status,
                                                    @RequestParam(required = false) String q,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return service.list(status, q, page, size);
    }

    @GetMapping("/{id}")
    public StaffLostItemResponse get(@PathVariable String id) {
        return service.get(id);
    }
}