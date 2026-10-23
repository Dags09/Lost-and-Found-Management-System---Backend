package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.UpdateProfileRequest;
import com.lostandfound.backend.dto.UserResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import com.lostandfound.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lets a Moderator or Admin correct a Student's profile. Students cannot edit their own profile.
 * Only Student accounts can be changed here; staff accounts are not touched by this flow.
 */
@Service
public class StudentProfileService {

    private final UserRepository users;

    public StudentProfileService(UserRepository users) {
        this.users = users;
    }

    /**
     * Updates the student's first name, last name and phone number. 404 if the id is unknown or is not a
     * Student account; 409 if the phone number belongs to another account. Email, student ID and password
     * are not changed here.
     */
    @Transactional
    public UserResponse update(String studentId, UpdateProfileRequest req) {
        Users u = users.findById(studentId)
                .filter(x -> x.getRole() == Role.Student)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        String phone = req.phoneNum();
        if (!phone.equals(u.getPhoneNum()) && users.existsByPhoneNum(phone))
            throw new ApiException(HttpStatus.CONFLICT, "That phone number is already used by another account");
        u.setFirstName(req.firstName().trim());
        u.setLastName(req.lastName().trim());
        u.setPhoneNum(phone);
        return UserResponse.from(users.save(u));
    }
}