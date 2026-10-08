package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.*;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import com.lostandfound.backend.repository.UserRepository;
import com.lostandfound.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Public sign-up always creates a Student. Staff accounts are created by an Admin. */
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        if (users.existsByPhoneNum(req.phoneNum())) throw new ApiException(HttpStatus.CONFLICT, "Phone number already registered");
        if (users.existsByStudentId(req.studentId())) throw new ApiException(HttpStatus.CONFLICT, "Student ID already registered");

        Users u = new Users();
        u.setFirstName(req.firstName().trim());
        u.setLastName(req.lastName().trim());
        u.setStudentId(req.studentId());
        u.setEmail(email);
        u.setPhoneNum(req.phoneNum());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.Student);
        return tokensFor(users.save(u));
    }

    public AuthResponse login(LoginRequest req) {
        Users u = users.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!encoder.matches(req.password(), u.getPassword()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return tokensFor(u);
    }

    public AuthResponse refresh(RefreshRequest req) {
        try {
            Claims claims = jwt.parseRefreshToken(req.refreshToken());
            Users u = users.findById(claims.getSubject())
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User no longer exists"));
            return tokensFor(u);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
    }

    public UserResponse me(String userId) {
        return users.findById(userId).map(UserResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AuthResponse tokensFor(Users u) {
        String role = u.getRole().name();
        return new AuthResponse(jwt.generateAccessToken(u.getId(), role),
                jwt.generateRefreshToken(u.getId(), role), UserResponse.from(u));
    }
}