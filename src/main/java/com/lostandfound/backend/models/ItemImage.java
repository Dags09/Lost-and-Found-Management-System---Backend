package com.lostandfound.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItemImage {
    // Points at a row in the "images" table (the bytes live there).
    @Column(name = "image_id", length = 36)
    private String imageId;
    private int position;

    public ItemImage() {}

    public ItemImage(String imageId, int position) {
        this.imageId = imageId;
        this.position = position;
    }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}