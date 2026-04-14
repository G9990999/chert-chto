package ru.mws.wiki.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration requests.
 *
 * @param username    unique login name (3–64 chars)
 * @param email       valid email address
 * @param password    password (8–128 chars)
 * @param displayName optional display name
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Email @Size(max = 128) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @Size(max = 128) String displayName
) {}
