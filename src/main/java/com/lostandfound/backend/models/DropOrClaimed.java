package com.lostandfound.backend.models;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.validation.constraints.NotBlank;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "drop_locations")
public class DropOrClaimed {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @NotBlank(message = "Required Name of the Place")
    private String name;

    @NotBlank(message = "Required contact number who to be claimed")
    private String contact;

    // The person the item is handed to at this place (e.g. the teacher). Shown only after approval.
    @Column(name = "person_name")
    private String personName;

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

    public String getPersonName() {
        return personName;
    }
    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
}