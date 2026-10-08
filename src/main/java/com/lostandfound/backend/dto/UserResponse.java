package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;

public record UserResponse(String id, String firstName, String lastName, Integer studentId,
                           String email, String phoneNum, Role role) {
    public static UserResponse from(Users u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getStudentId(),
                u.getEmail(), u.getPhoneNum(), u.getRole());
    }
}