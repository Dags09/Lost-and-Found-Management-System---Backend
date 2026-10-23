package com.lostandfound.backend.models.enumRoleStatusTypes;

/**
 * Life of an item or lost report. OPEN = active, CLAIMED = owner found, CLOSED = the reporter closed
 * it, EXPIRED = nobody claimed it in time. MATCHED is for LOST reports only: the report has at least
 * one suggested found item waiting for the owner. It goes back to OPEN when no suggestion is left.
 * A MATCHED report behaves like an OPEN one (can be edited, closed, expire and be claimed against).
 */
public enum ItemStatus {
    OPEN, MATCHED, CLAIMED, CLOSED, EXPIRED
}
