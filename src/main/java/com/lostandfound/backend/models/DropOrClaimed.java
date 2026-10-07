package com.lostandfound.backend.models;

import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "DropOrClaimed")
public class DropOrClaimed {

    @Id
    private ObjectId id;

    @NotBlank(message = "Required Name of the Place")
    private String name;

    @NotBlank(message = "Required contact number who to be claimed")
    private String contact;

    public DropOrClaimed(){}

    //getters

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    //setters

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
