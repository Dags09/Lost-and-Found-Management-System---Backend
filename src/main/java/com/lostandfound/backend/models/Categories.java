package com.lostandfound.backend.models;

import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "categories")
public class Categories {
    @Id
    private ObjectId id;

    @NotBlank(message = "Category name is required")
    private String name;


    private String icon;


    public Categories(){}

    //getters
    public ObjectId getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    //setters

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
