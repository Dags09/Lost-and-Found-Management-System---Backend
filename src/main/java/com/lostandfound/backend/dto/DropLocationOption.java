package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.DropOrClaimed;


public record DropLocationOption(String id, String name) {
    public static DropLocationOption from(DropOrClaimed d) {
        return new DropLocationOption(d.getId(), d.getName());
    }
}