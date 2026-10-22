package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;

/** Safe view of a user. Never includes the password hash. */
public record UserResponse(
        String id,
        String firstName,
        String lastName,
        String studentId,
        String email,
        String phoneNum,
        Role role
) {
    public static UserResponse from(Users u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getStudentId(),
                u.getEmail(), u.getPhoneNum(), u.getRole());
    }
}