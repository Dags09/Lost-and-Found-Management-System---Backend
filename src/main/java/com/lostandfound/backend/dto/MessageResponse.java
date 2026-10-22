package com.lostandfound.backend.dto;

/** Plain {"message": ...} reply for endpoints that only need to say what happened. */
public record MessageResponse(String message) {}