package ru.mws.wiki.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for login requests.
 *
 * @param username the user's login name
 * @param password the user's password (plaintext, compared against BCrypt hash)
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
