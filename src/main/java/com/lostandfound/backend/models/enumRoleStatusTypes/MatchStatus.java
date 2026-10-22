package com.lostandfound.backend.models.enumRoleStatusTypes;

/**
 * SUGGESTED = shown to the owner. CONFIRMED = the claim was approved. DISMISSED = owner said
 * "not mine", or the item is gone.
 */
public enum MatchStatus {
    SUGGESTED, CONFIRMED, DISMISSED
}
