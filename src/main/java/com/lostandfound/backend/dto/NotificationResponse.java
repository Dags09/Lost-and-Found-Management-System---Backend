package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Notification;
import com.lostandfound.backend.models.enumRoleStatusTypes.NotificationType;

import java.time.Instant;
import java.util.Map;

/**
 * One in-app notification. payload carries the details (event, message, itemId, ...) so the app
 * can open the right screen.
 */
public record NotificationResponse(
        String id,
        NotificationType type,
        Map<String, Object> payload,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getPayload(), n.isRead(), n.getCreatedAt());
    }
}