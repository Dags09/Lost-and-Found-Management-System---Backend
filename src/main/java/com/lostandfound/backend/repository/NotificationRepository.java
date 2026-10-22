package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for in-app notifications. */
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /** A user's notifications, newest first, one page at a time. */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /** Unread count for the badge. */
    long countByUserIdAndReadFalse(String userId);

    // One UPDATE for all of a user's unread notifications, instead of loading each one.
    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.read = true where n.userId = :userId and n.read = false")
    int markAllRead(@Param("userId") String userId);
}