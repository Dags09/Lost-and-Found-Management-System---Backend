package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.NotificationResponse;
import com.lostandfound.backend.dto.PageResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Notification;
import com.lostandfound.backend.models.enumRoleStatusTypes.NotificationType;
import com.lostandfound.backend.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    /**
     * Saves one CLAIM_UPDATE notification. It joins the caller's transaction, so if an approval
     * rolls back, its notifications roll back with it. A null userId is skipped.
     * "event" tells the app what happened (CLAIM_SUBMITTED, CLAIM_APPROVED, CLAIM_REJECTED, ITEM_CLAIMED).
     */
    @Transactional
    public void claimUpdate(String userId, String event, String message, String itemId, String claimId) {
        if (userId == null) return;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event);
        payload.put("message", message);
        if (itemId != null) payload.put("itemId", itemId);
        if (claimId != null) payload.put("claimId", claimId);

        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(NotificationType.CLAIM_UPDATE);
        n.setPayload(payload);
        notifications.save(n);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(String userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<Notification> result = notifications.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(safePage, safeSize));
        List<NotificationResponse> content = result.getContent().stream().map(NotificationResponse::from).toList();
        return new PageResponse<>(content, safePage, safeSize,
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public long unreadCount(String userId) {
        return notifications.countByUserIdAndReadFalse(userId);
    }

    /** Someone else's notification looks exactly like a missing one: 404. */
    @Transactional
    public NotificationResponse markRead(String id, String userId) {
        Notification n = notifications.findById(id)
                .filter(x -> userId.equals(x.getUserId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        n.setRead(true);
        return NotificationResponse.from(notifications.save(n));
    }

    @Transactional
    public int markAllRead(String userId) {
        return notifications.markAllRead(userId);
    }
}