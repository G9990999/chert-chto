package ru.mws.wiki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for creating or updating a wiki page.
 *
 * @param title        page title (required, max 255)
 * @param content      TipTap JSON content
 * @param publicPage   whether the page is visible to all authenticated users
 * @param linkedPageIds UUIDs of pages this page links to
 */
public record PageRequest(
        @NotBlank @Size(max = 255) String title,
        String content,
        boolean publicPage,
        Set<UUID> linkedPageIds
) {}
