package com.lostandfound.backend.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "students")
public class Students {

    @Id
    private ObjectId id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;


    @NotBlank
    @Indexed(unique = true)
    private int studentId;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    @Indexed(unique = true)
    private int phoneNum;

    @NotBlank
    private String password;

    @NotBlank
    private Role role = Role.Student;

    @CreatedDate
    private Instant createdAt = Instant.now();


    public Students(){}

    //getters
    public ObjectId getId(){
        return id;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public int getStudentId(){
        return studentId;
    }
    public String getEmail(){
        return email;
    }
    public int getPhoneNum(){
        return phoneNum;
    }
    public String getPassword(){
        return password;
    }
    public Role getRole(){
        return role;
    }
    public Instant getCreatedAt(){
        return createdAt;
    }

    //setters

    public void setId(ObjectId id){
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNum(int phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
