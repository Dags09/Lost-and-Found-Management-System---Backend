package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.NotificationResponse;
import com.lostandfound.backend.dto.PageResponse;
import com.lostandfound.backend.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/** /v1/notifications: the logged-in user's own in-app notifications. */
@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    /** GET /v1/notifications : newest first. */
    @GetMapping
    public PageResponse<NotificationResponse> list(Authentication authentication,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return service.list((String) authentication.getPrincipal(), page, size);
    }


    /** GET /v1/notifications/unread-count : {"count": n} for the badge. */
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        return Map.of("count", service.unreadCount((String) authentication.getPrincipal()));
    }

    /** POST /v1/notifications/{id}/read */
    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable String id, Authentication authentication) {
        return service.markRead(id, (String) authentication.getPrincipal());
    }

    /** POST /v1/notifications/read-all : {"updated": n} */
    @PostMapping("/read-all")
    public Map<String, Integer> markAllRead(Authentication authentication) {
        return Map.of("updated", service.markAllRead((String) authentication.getPrincipal()));
    }
}