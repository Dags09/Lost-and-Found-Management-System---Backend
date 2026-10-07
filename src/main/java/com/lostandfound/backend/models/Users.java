package com.lostandfound.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
public class Users {

    @Id
    private ObjectId id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Indexed(unique = true, sparse = true)
    private Integer studentId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Indexed(unique = true)
    private String email;


    @NotBlank(message = "Phone number is required")
    @Indexed(unique = true)
    private String phoneNum;

    @NotBlank(message = "Password is required")
    private String password;


    @NotNull
    private Role role = Role.Student;

    @CreatedDate
    private Instant createdAt = Instant.now();

    public Users(){}

    // Getters
    public ObjectId getId(){ return id; }
    public String getFirstName(){ return firstName; }
    public String getLastName(){ return lastName; }
    public Integer getStudentId(){ return studentId; }
    public String getEmail(){ return email; }
    public String getPhoneNum(){ return phoneNum; }
    public String getPassword(){ return password; }
    public Role getRole(){ return role; }
    public Instant getCreatedAt(){ return createdAt; }

    // Setters
    public void setId(ObjectId id){ this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }


    // Validation Conditions

    @JsonIgnore
    @AssertTrue(message = "Student ID is required for Student accounts")
    public boolean isStudentIdValidForRole() {

        if (this.role == Role.Student) {
            return this.studentId != null && this.studentId > 0;
        }

        return true;
    }
}