package com.ai.audio.transcriber.dto;

/**
 * DTO for sending authentication results (token and message) back to the client.
 */
public record AuthResponse(
        String token,
        String message
) {}