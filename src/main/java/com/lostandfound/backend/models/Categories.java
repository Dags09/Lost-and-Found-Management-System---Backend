package com.lostandfound.backend.models;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.validation.constraints.NotBlank;


/** Item category (Phones, Wallets, ...). The name is unique ignoring case (see schema.sql). */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "categories")
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @NotBlank(message = "Category name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Icon is required")
    @Column(nullable = false)
    private String icon;


    public Categories(){}

    // Getters & Setters

    public String getId(){
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


    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }

}