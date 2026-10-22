package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.PageResponse;
import com.lostandfound.backend.dto.StaffCreateRequest;
import com.lostandfound.backend.dto.UserResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import com.lostandfound.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminUserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /** Newest first. Pass a role to see only Students, Moderators or Admins. */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Role role, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());

        Page<Users> result = role == null ? users.findAll(pageable) : users.findByRole(role, pageable);
        List<UserResponse> content = result.getContent().stream().map(UserResponse::from).toList();
        return new PageResponse<>(content, safePage, safeSize,
                result.getTotalElements(), result.getTotalPages());
    }

    /** Creates a Moderator or Admin account. Students sign themselves up, so Student is refused here. */
    @Transactional
    public UserResponse createStaff(StaffCreateRequest req) {
        if (req.role() == Role.Student)
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Choose Moderator or Admin. Students register themselves");

        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email) || users.existsByPhoneNum(req.phoneNum()))
            throw new ApiException(HttpStatus.CONFLICT, "An account with these details already exists");

        Users u = new Users();
        u.setFirstName(req.firstName().trim());
        u.setLastName(req.lastName().trim());
        u.setEmail(email);
        u.setPhoneNum(req.phoneNum());
        u.setPassword(encoder.encode(req.password()));
        u.setEmailVerified(true);   // created by an Admin, so the email is trusted
        u.setRole(req.role());
        return UserResponse.from(users.save(u));
    }

    /**
     * Promote or demote an existing account. You cannot change your own role, so the
     * last Admin can never lock everyone out by accident.
     */
    @Transactional
    public UserResponse changeRole(String userId, String adminId, Role role) {
        if (userId.equals(adminId))
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        Users u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        u.setRole(role);
        return UserResponse.from(users.save(u));
    }
}