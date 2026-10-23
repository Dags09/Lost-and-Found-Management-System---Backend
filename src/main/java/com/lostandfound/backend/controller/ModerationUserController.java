package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.UpdateProfileRequest;
import com.lostandfound.backend.dto.UserResponse;
import com.lostandfound.backend.service.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** /v1/moderation/users: staff (Moderator or Admin) edit a student's profile. */
@RestController
@RequestMapping("/v1/moderation/users")
public class ModerationUserController {

    private final StudentProfileService service;

    public ModerationUserController(StudentProfileService service) {
        this.service = service;
    }

    /** PUT /v1/moderation/users/{id} : edit a student's first name, last name and phone number. */
    @PutMapping("/{id}")
    public UserResponse updateStudent(@PathVariable String id, @Valid @RequestBody UpdateProfileRequest req) {
        return service.update(id, req);
    }
}