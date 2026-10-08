package com.lostandfound.backend.models;

public class ItemImage {

    private String url;
    private int position;

    public ItemImage() {}

    public ItemImage(String url, int position) {
        this.url = url;
        this.position = position;
    }


    // Getters & Setters

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
