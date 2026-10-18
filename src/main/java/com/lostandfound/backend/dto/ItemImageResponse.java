package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Items;

import java.util.List;

public record ItemImageResponse(String url, int position) {
    public static List<ItemImageResponse> listFrom(Items i) {
        return i.getImages().stream()
                .map(img -> new ItemImageResponse(img.getUrl(), img.getPosition()))
                .toList();
    }
}