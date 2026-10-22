package com.lostandfound.backend.service;

import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
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

@Service
public class ItemExpiryService {

    private static final Logger log = LoggerFactory.getLogger(ItemExpiryService.class);

    private final ItemRepository items;
    private final NotificationService notifications;
    private final TransactionTemplate tx;

    @Value("${app.item-expiry.days:90}")
    private long foundDays;

    @Value("${app.item-expiry.lost-days:180}")
    private long lostDays;

    @Value("${app.item-expiry.batch-size:200}")
    private int batchSize;

    public ItemExpiryService(ItemRepository items, NotificationService notifications, TransactionTemplate tx) {
        this.items = items;
        this.notifications = notifications;
        this.tx = tx;
    }

    @Scheduled(cron = "${app.item-expiry.cron:0 0 3 * * *}")
    public void runScheduled() {
        int found = expire(ItemType.FOUND, foundDays);
        int lost = expire(ItemType.LOST, lostDays);
        if (found + lost > 0)
            log.info("Expired {} unclaimed found item(s) and {} unresolved lost report(s)", found, lost);
    }

    /** Expires every stale found item and lost report. Returns how many were expired in all. */
    public int expireStaleItems() {
        return expire(ItemType.FOUND, foundDays) + expire(ItemType.LOST, lostDays);
    }

    private int expire(ItemType type, long days) {
        Instant now = Instant.now();
        Instant cutoff = now.minus(Duration.ofDays(days));
        int size = Math.max(batchSize, 1);
        int total = 0;
        List<String> ids;
        do {
            ids = items.findExpirableIds(type, cutoff, PageRequest.of(0, size));
            int expiredInBatch = 0;
            for (String id : ids) {
                try {
                    Boolean done = tx.execute(status -> expireOne(id, type, days, cutoff, now));
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

    private boolean expireOne(String id, ItemType type, long days, Instant cutoff, Instant now) {
        if (items.markExpiredIfStale(id, type, cutoff, now) != 1) return false;
        items.findById(id).ifPresent(i -> notifications.claimUpdate(
                i.getReporterId(), "ITEM_EXPIRED",
                type == ItemType.FOUND
                        ? "Your found item \"" + i.getName() + "\" was closed because nobody claimed it in "
                        + days + " days."
                        : "Your lost report \"" + i.getName() + "\" was closed after " + days
                        + " days. File a new report if you are still looking for it.",
                id, null));
        return true;
    }
}