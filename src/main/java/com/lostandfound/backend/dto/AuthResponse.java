package com.lostandfound.backend.dto;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
