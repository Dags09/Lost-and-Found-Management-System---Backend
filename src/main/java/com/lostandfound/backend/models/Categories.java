package com.lostandfound.backend.models;

import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "categories")
public class Categories {
    @Id
    private ObjectId id;

    @NotBlank(message = "Category name is required")
    @Indexed(unique = true)
    private String name;

    @NotBlank(message = "Icon is required")
    private String icon;


    public Categories(){}

    // Getters & Setters

    public ObjectId getId(){
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }

}
