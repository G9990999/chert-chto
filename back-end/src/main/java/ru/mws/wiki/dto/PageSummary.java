package ru.mws.wiki.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight page summary used in list views.
 *
 * @param id          page UUID
 * @param title       page title
 * @param authorName  display name of the author
 * @param updatedAt   last modification timestamp
 * @param publicPage  whether page is public
 */
public record PageSummary(
        UUID id,
        String title,
        String authorName,
        Instant updatedAt,
        boolean publicPage
) {}
