package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.PageResponse;
import com.lostandfound.backend.dto.RoleChangeRequest;
import com.lostandfound.backend.dto.StaffCreateRequest;
import com.lostandfound.backend.dto.UserResponse;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import com.lostandfound.backend.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Admin only. SecurityConfig already limits /v1/admin/** to Admin; @PreAuthorize is a second lock. */
@RestController
@RequestMapping("/v1/admin/users")
@PreAuthorize("hasRole('Admin')")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(required = false) Role role,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return service.list(role, page, size);
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createStaff(@Valid @RequestBody StaffCreateRequest req) {
        return service.createStaff(req);
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable String id, Authentication authentication,
                                   @Valid @RequestBody RoleChangeRequest req) {
        return service.changeRole(id, (String) authentication.getPrincipal(), req.role());
    }
}