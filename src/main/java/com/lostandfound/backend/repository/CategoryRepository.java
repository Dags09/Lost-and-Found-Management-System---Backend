package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Categories;
import org.springframework.data.jpa.repository.JpaRepository;




public interface CategoryRepository extends JpaRepository<Categories, String> {
    boolean existsByNameIgnoreCase(String name);
}
//public interface CategoryRepository extends JpaRepository<Categories, String>
//
//interface means this file only declares what it can do and contains no code that does it.
//You never write the code. At startup, Spring sees this interface and generates the working code for you.
//
//extends JpaRepository<Categories, String> means “inherit everything MongoRepository can do”.
//The two things in <...> tell it:
//
//Categories: which model this repository manages (the categories collection).
//String: the type of the id field. Your Categories class has private String id.

//boolean existsByNameIgnoreCase(String name);
//
//This is a custom method. You write only its name, and Spring builds the database query from it. It reads the name in parts:
//
//exists   By   Name   IgnoreCase
//  │       │     │        │
//  │       │     │        └─ don't care about upper/lower case
//  │       │     └─ look at the "name" field
//  │       └─ "where"
//  └─ answer true/false: is there at least one?
//
//So existsByNameIgnoreCase("phones") asks MongoDB: is there a category whose name is “phones”,
// capitals? It returns true for an existing “Phones”, “PHONES” or “phones”.
//
//Why IgnoreCase? The unique index on name in your model is case-sensitive. Without this check,
// someone could create “Phones” and then “phones”, and both would be accepted. The service uses this method to block that and return the 409 error