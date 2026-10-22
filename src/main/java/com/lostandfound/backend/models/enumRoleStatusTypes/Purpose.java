package com.lostandfound.backend.models.enumRoleStatusTypes;

public enum Purpose {
    ITEM,        // photo of a found item: public
    PROOF,       // claim proof: only the claimant and staff
    LOST_ITEM    // photo on a lost report: only its owner and staff
}