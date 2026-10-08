package com.lostandfound.backend.models;

import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "items")
public class Items {

    @Id
    private ObjectId id;

    @Indexed
    private String reporterId;

    @NotBlank(message = "Required name for the item")
    private String name;

    @NotBlank(message = "Required item type")
    @Indexed
    private ItemType type;

    @NotBlank(message = "Required description for the item")
    @TextIndexed
    private String description;

    @Indexed
    private String categoryId;

    @NotBlank(message = "Required color for the item")
    private String color;

    @NotBlank(message = "Required brand for the item")
    private String brand;

    //do not reveal this in public, this should be used to identify who is the owner
    @NotBlank(message = "Required distinguishing marks for the item")
    private String distinguishingMarks;

    @NotBlank(message = "Required location where it found")
    private String locationName;

    private LocalDate dateOccurred;


    @Indexed
    private ItemStatus status = ItemStatus.OPEN;

    private List<ItemImage> images = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Items(){}

    // Getters & Setters

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }


    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }


    public ItemType getType() {
        return type;
    }
    public void setType(ItemType type) {
        this.type = type;
    }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }


    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


    public String getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }


    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }


    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }


    public String getDistinguishingMarks() {
        return distinguishingMarks;
    }
    public void setDistinguishingMarks(String distinguishingMarks) {
        this.distinguishingMarks = distinguishingMarks;
    }


    public String getLocationName() {
        return locationName;
    }
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }


    public LocalDate getDateOccurred() {
        return dateOccurred;
    }
    public void setDateOccurred(LocalDate dateOccurred) {
        this.dateOccurred = dateOccurred;
    }


    public ItemStatus getStatus() {
        return status;
    }
    public void setStatus(ItemStatus status) {
        this.status = status;
    }


    public List<ItemImage> getImages() {
        return images;
    }
    public void setImages(List<ItemImage> images) {
        this.images = images;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }


    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
