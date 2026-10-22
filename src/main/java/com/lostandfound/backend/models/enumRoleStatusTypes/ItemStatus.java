package com.lostandfound.backend.models.enumRoleStatusTypes;

/**
 * Life of an item or lost report. OPEN = active, CLAIMED = owner found, CLOSED = the reporter closed
 * it, EXPIRED = nobody claimed it in time. MATCHED is not used yet.
 */
public enum ItemStatus {
    OPEN, MATCHED, CLAIMED, CLOSED, EXPIRED
}
