package com.lostandfound.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "student_id", unique = true)
    private String studentId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;


    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_num", nullable = false, unique = true)
    private String phoneNum;

    @NotBlank(message = "Password is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, max = 12)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,12}$",
            message = "Password must be 8–12 characters with at least one letter and one number")
    private String password;


    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role = Role.Student;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    public Users(){}

    // Getters & Setters
    public String getId(){ return id; }
    public void setId(String id){ this.id = id; }

    public String getFirstName(){ return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName(){ return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Integer getStudentId(){ return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getEmail(){ return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNum(){ return phoneNum; }
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }

    public String getPassword(){ return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole(){ return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getCreatedAt(){ return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }


    // Validation Conditions

    //only users that has a role of student are required to put a student id
    @JsonIgnore
    @AssertTrue(message = "Student ID is required for Student accounts")
    public boolean isStudentIdValidForRole() {

        if (this.role == Role.Student) {
            return this.studentId != null && this.studentId > 0;
        }

        return true;
    }
}