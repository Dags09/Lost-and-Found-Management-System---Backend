package com.lostandfound.backend.models;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "DropOrClaimed")
public class DropOrClaimed {

    @Id
    private String id;

    @NotBlank(message = "Required Name of the Place")
    private String name;

    @NotBlank(message = "Required contact number who to be claimed")
    private String contact;

    public DropOrClaimed(){}

    // Getters & Setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
}