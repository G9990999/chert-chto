package ru.mws.wiki.dto;

import ru.mws.wiki.entity.Role;

import java.util.UUID;

/**
 * DTO returned after successful authentication (login or register).
 *
 * @param token       JWT bearer token
 * @param userId      authenticated user's UUID
 * @param username    username
 * @param role        assigned role
 * @param displayName optional display name
 */
public record AuthResponse(
        String token,
        UUID userId,
        String username,
        Role role,
        String displayName
) {}
