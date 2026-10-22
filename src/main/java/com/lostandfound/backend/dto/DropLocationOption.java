package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.DropOrClaimed;


/**
 * Drop-off place name only. Safe to show every logged-in user; the contact person and number
 * stay Admin-only (see DropLocationController.details).
 */
public record DropLocationOption(String id, String name) {
    public static DropLocationOption from(DropOrClaimed d) {
        return new DropLocationOption(d.getId(), d.getName());
    }
}