package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.NotificationResponse;
import com.lostandfound.backend.dto.PageResponse;
import com.lostandfound.backend.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Every logged-in user sees only their own notifications. */
@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<NotificationResponse> list(Authentication authentication,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return service.list((String) authentication.getPrincipal(), page, size);
    }

    // For the red badge on the bell icon.
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        return Map.of("count", service.unreadCount((String) authentication.getPrincipal()));
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable String id, Authentication authentication) {
        return service.markRead(id, (String) authentication.getPrincipal());
    }

    @PostMapping("/read-all")
    public Map<String, Integer> markAllRead(Authentication authentication) {
        return Map.of("updated", service.markAllRead((String) authentication.getPrincipal()));
    }
}