package com.ai.audio.transcriber.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user login requests.
 * Uses Java Records for conciseness (since Java 16+).
 */
public record LoginRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password cannot be empty")
        String password
) {}