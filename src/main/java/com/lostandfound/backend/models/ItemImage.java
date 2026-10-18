package com.lostandfound.backend.models;

public class ItemImage {
    private String url;
    private String publicId;   // Cloudinary's id, needed to delete the file. Never sent to clients.
    private int position;

    public ItemImage() {}

    public ItemImage(String url, String publicId, int position) {
        this.url = url;
        this.publicId = publicId;
        this.position = position;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}