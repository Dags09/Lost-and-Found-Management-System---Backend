package com.lostandfound.backend.service;

import com.lostandfound.backend.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Closes old, unclaimed FOUND items on its own (OPEN -> EXPIRED) so the public list does not grow forever.
 * An item is skipped while it has a PENDING claim. Each item is expired in its own transaction, so one
 * failure never undoes the others, and the update re-checks the rules, so two app instances running
 * this at once cannot expire (or notify) the same item twice.
 */
@Service
public class ItemExpiryService {

    private static final Logger log = LoggerFactory.getLogger(ItemExpiryService.class);

    private final ItemRepository items;
    private final NotificationService notifications;
    private final TransactionTemplate tx;

    @Value("${app.item-expiry.days:90}")
    private long expiryDays;

    @Value("${app.item-expiry.batch-size:200}")
    private int batchSize;

    public ItemExpiryService(ItemRepository items, NotificationService notifications, TransactionTemplate tx) {
        this.items = items;
        this.notifications = notifications;
        this.tx = tx;
    }

    @Scheduled(cron = "${app.item-expiry.cron:0 0 3 * * *}")
    public void runScheduled() {
        int n = expireStaleItems();
        if (n > 0) log.info("Expired {} unclaimed found item(s)", n);
    }

    /** Expires every found item older than the configured age. Returns how many were expired. */
    public int expireStaleItems() {
        Instant now = Instant.now();
        Instant cutoff = now.minus(Duration.ofDays(expiryDays));
        int size = Math.max(batchSize, 1);
        int total = 0;
        List<String> ids;
        do {
            ids = items.findExpirableIds(cutoff, PageRequest.of(0, size));
            int expiredInBatch = 0;
            for (String id : ids) {
                try {
                    Boolean done = tx.execute(status -> expireOne(id, cutoff, now));
                    if (Boolean.TRUE.equals(done)) expiredInBatch++;
                } catch (RuntimeException e) {
                    log.warn("Could not expire item {}", id, e);
                }
            }
            total += expiredInBatch;
            // Stop if nothing in this batch changed (all raced or failed), otherwise we would loop forever.
            if (expiredInBatch == 0) break;
        } while (ids.size() >= size);
        return total;
    }

    private boolean expireOne(String id, Instant cutoff, Instant now) {
        if (items.markExpiredIfStale(id, cutoff, now) != 1) return false;
        items.findById(id).ifPresent(i -> notifications.claimUpdate(
                i.getReporterId(), "ITEM_EXPIRED",
                "Your found item \"" + i.getName() + "\" was closed because nobody claimed it in "
                        + expiryDays + " days.",
                id, null));
        return true;
    }
}