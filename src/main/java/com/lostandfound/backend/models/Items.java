package com.lostandfound.backend.models;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * One report in the items table: either a FOUND item or a LOST report (see type). They share one
 * table so both can be searched and matched using the same fields.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "items")
public class Items {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(name = "reporter_id")
    private String reporterId;

    @NotBlank(message = "Required name for the item")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Required item type")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @NotBlank(message = "Required description for the item")
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "category_id")
    private String categoryId;

    @NotBlank(message = "Required color for the item")
    @Column(nullable = false)
    private String color;

    @NotBlank(message = "Required brand for the item")
    @Column(nullable = false)
    private String brand;

    //do not reveal this in public, this should be used to identify who is the owner
    @NotBlank(message = "Required distinguishing marks for the item")
    @Column(name = "distinguishing_marks", nullable = false, columnDefinition = "text")
    private String distinguishingMarks;

    @NotBlank(message = "Required location where it found")
    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(name = "drop_location_id")
    private String dropLocationId;

    @Column(name = "date_occurred")
    private LocalDate dateOccurred;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.OPEN;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @OrderBy("position ASC")
    private List<ItemImage> images = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Items(){}

    // Getters & Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


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


    public String getDropLocationId() { return dropLocationId; }
    public void setDropLocationId(String dropLocationId) { this.dropLocationId = dropLocationId; }


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