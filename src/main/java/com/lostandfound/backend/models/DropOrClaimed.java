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
}
