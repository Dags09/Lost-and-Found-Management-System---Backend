package com.lostandfound.backend.models;

import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "items")
public class Items {

    @Id
    private ObjectId id;

    @NotBlank(message = "Required Name for the item")
    private String name;

}
