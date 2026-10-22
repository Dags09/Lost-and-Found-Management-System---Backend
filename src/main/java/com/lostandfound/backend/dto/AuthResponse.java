package com.lostandfound.backend.dto;

/**
 * Result of login, register and refresh. accessToken and refreshToken are null after register,
 * because the account cannot log in until its email is verified.
 */
public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
