package ru.mws.wiki.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mws.wiki.dto.PageRequest;
import ru.mws.wiki.dto.PageResponse;
import ru.mws.wiki.dto.PageSummary;
import ru.mws.wiki.service.PageService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * REST controller for wiki page CRUD operations.
 *
 * <p>Role-based access is partially enforced by Spring Security config
 * and further controlled with {@code @PreAuthorize} at method level.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    /**
     * Lists all pages accessible to the authenticated user.
     *
     * @return list of page summaries
     */
    @GetMapping
    public List<PageSummary> list() {
        return pageService.listAccessible();
    }

    /**
     * Searches pages by title.
     *
     * @param q search query
     * @return matching page summaries
     */
    @GetMapping("/search")
    public List<PageSummary> search(@RequestParam String q) {
        return pageService.search(q);
    }

    /**
     * Retrieves a full page by ID.
     *
     * @param id page UUID
     * @return page response with content and backlinks
     */
    @GetMapping("/{id}")
    public PageResponse get(@PathVariable UUID id) {
        return pageService.getById(id);
    }

    /**
     * Creates a new wiki page.
     *
     * @param request page data (validated)
     * @return created page response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PageResponse create(@Valid @RequestBody PageRequest request) {
        return pageService.create(request);
    }

    /**
     * Updates an existing page.
     *
     * @param id      page UUID
     * @param request updated data (validated)
     * @return updated page response
     */
    @PutMapping("/{id}")
    public PageResponse update(@PathVariable UUID id, @Valid @RequestBody PageRequest request) {
        return pageService.update(id, request);
    }

    /**
     * Deletes a page (ADMIN only).
     *
     * @param id page UUID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        pageService.delete(id);
    }

    /**
     * Shares a page with specified users.
     *
     * @param id      page UUID
     * @param userIds set of user UUIDs
     */
    @PostMapping("/{id}/share")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void share(@PathVariable UUID id, @RequestBody Set<UUID> userIds) {
        pageService.share(id, userIds);
    }
}
