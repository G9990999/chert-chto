package ru.mws.wiki.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO returned when fetching a wiki page.
 *
 * @param id          page UUID
 * @param title       page title
 * @param content     TipTap JSON content
 * @param authorId    UUID of the author
 * @param authorName  display name of the author
 * @param createdAt   creation timestamp
 * @param updatedAt   last modification timestamp
 * @param version     optimistic locking version
 * @param publicPage  whether page is public
 * @param linkedPageIds outgoing links (UUIDs)
 * @param backlinks   incoming links (UUIDs of pages that reference this page)
 */
public record PageResponse(
        UUID id,
        String title,
        String content,
        UUID authorId,
        String authorName,
        Instant createdAt,
        Instant updatedAt,
        Long version,
        boolean publicPage,
        Set<UUID> linkedPageIds,
        Set<UUID> backlinks
) {}
