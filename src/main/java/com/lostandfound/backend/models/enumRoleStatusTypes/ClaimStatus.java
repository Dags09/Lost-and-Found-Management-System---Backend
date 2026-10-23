package com.lostandfound.backend.models.enumRoleStatusTypes;

/**
 * Life of a claim: PENDING, then APPROVED or REJECTED by staff, or CANCELLED by the student
 * while it is still PENDING.
 */
public enum ClaimStatus {
    PENDING, APPROVED, REJECTED, CANCELLED
}