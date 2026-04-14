package ru.mws.wiki.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mws.wiki.dto.PageRequest;
import ru.mws.wiki.dto.PageResponse;
import ru.mws.wiki.dto.PageSummary;
import ru.mws.wiki.entity.Page;
import ru.mws.wiki.entity.User;
import ru.mws.wiki.exception.AccessDeniedException;
import ru.mws.wiki.exception.ResourceNotFoundException;
import ru.mws.wiki.repository.PageRepository;
import ru.mws.wiki.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for CRUD operations on wiki pages.
 *
 * <p>Integrates with Caffeine cache for GET performance and broadcasts
 * real-time updates via STOMP WebSocket upon writes.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Lists all pages accessible to the currently authenticated user.
     *
     * @return list of page summaries
     */
    @Transactional(readOnly = true)
    @Cacheable("pageList")
    public List<PageSummary> listAccessible() {
        User user = currentUser();
        return pageRepository.findAccessibleByUser(user).stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Retrieves a single page by ID, enforcing access rules.
     *
     * @param id page UUID
     * @return full page response including backlinks
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "pages", key = "#id")
    public PageResponse getById(UUID id) {
        Page page = findPage(id);
        checkAccess(page);
        return toResponse(page);
    }

    /**
     * Creates a new page for the current user.
     *
     * @param request page data
     * @return created page response
     */
    @Transactional
    @CacheEvict(value = "pageList", allEntries = true)
    public PageResponse create(PageRequest request) {
        User user = currentUser();
        Page page = Page.builder()
                .title(request.title())
                .content(request.content())
                .author(user)
                .publicPage(request.publicPage())
                .linkedPageIds(request.linkedPageIds() != null ? request.linkedPageIds() : Set.of())
                .build();
        pageRepository.save(page);
        log.info("Page created: {} by {}", page.getId(), user.getUsername());
        return toResponse(page);
    }

    /**
     * Updates an existing page. Only the author or an ADMIN may update.
     *
     * @param id      page UUID
     * @param request updated page data
     * @return updated page response
     */
    @Transactional
    @CacheEvict(value = {"pages", "pageList"}, allEntries = true)
    public PageResponse update(UUID id, PageRequest request) {
        Page page = findPage(id);
        checkEditAccess(page);
        page.setTitle(request.title());
        page.setContent(request.content());
        page.setPublicPage(request.publicPage());
        if (request.linkedPageIds() != null) {
            page.setLinkedPageIds(request.linkedPageIds());
        }
        pageRepository.save(page);
        // Broadcast update to all collaborative editors
        messagingTemplate.convertAndSend("/topic/pages/" + id, toResponse(page));
        log.info("Page updated: {} by {}", id, currentUsername());
        return toResponse(page);
    }

    /**
     * Deletes a page (ADMIN only, enforced at controller level).
     *
     * @param id page UUID
     */
    @Transactional
    @CacheEvict(value = {"pages", "pageList"}, allEntries = true)
    public void delete(UUID id) {
        Page page = findPage(id);
        pageRepository.delete(page);
        log.info("Page deleted: {} by {}", id, currentUsername());
    }

    /**
     * Shares a page with a set of users.
     *
     * @param id      page UUID
     * @param userIds set of user UUIDs to share with
     */
    @Transactional
    @CacheEvict(value = "pages", key = "#id")
    public void share(UUID id, Set<UUID> userIds) {
        Page page = findPage(id);
        checkEditAccess(page);
        Set<User> shareTargets = userIds.stream()
                .map(uid -> userRepository.findById(uid)
                        .orElseThrow(() -> new ResourceNotFoundException("User", uid)))
                .collect(Collectors.toSet());
        page.getSharedWith().addAll(shareTargets);
        pageRepository.save(page);
        log.info("Page {} shared with {} users", id, userIds.size());
    }

    /**
     * Searches pages by title (case-insensitive).
     *
     * @param query search string
     * @return list of matching page summaries
     */
    @Transactional(readOnly = true)
    public List<PageSummary> search(String query) {
        return pageRepository.findByTitleContainingIgnoreCase(query).stream()
                .filter(p -> isAccessible(p, currentUser()))
                .map(this::toSummary)
                .toList();
    }

    // ---- Private helpers ----

    private Page findPage(UUID id) {
        return pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page", id));
    }

    private void checkAccess(Page page) {
        User user = currentUser();
        if (!isAccessible(page, user)) {
            throw new AccessDeniedException("You do not have access to this page");
        }
    }

    private boolean isAccessible(Page page, User user) {
        return page.isPublicPage()
                || page.getAuthor().getId().equals(user.getId())
                || page.getSharedWith().stream().anyMatch(u -> u.getId().equals(user.getId()));
    }

    private void checkEditAccess(Page page) {
        User user = currentUser();
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isAdmin && !page.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the author or an admin can edit this page");
        }
    }

    private User currentUser() {
        String username = currentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private PageSummary toSummary(Page page) {
        return new PageSummary(
                page.getId(),
                page.getTitle(),
                page.getAuthor().getDisplayName(),
                page.getUpdatedAt(),
                page.isPublicPage()
        );
    }

    private PageResponse toResponse(Page page) {
        Set<UUID> backlinks = pageRepository
                .findByLinkedPageIdsContaining(page.getId())
                .stream()
                .map(Page::getId)
                .collect(Collectors.toSet());
        return new PageResponse(
                page.getId(),
                page.getTitle(),
                page.getContent(),
                page.getAuthor().getId(),
                page.getAuthor().getDisplayName(),
                page.getCreatedAt(),
                page.getUpdatedAt(),
                page.getVersion(),
                page.isPublicPage(),
                page.getLinkedPageIds(),
                backlinks
        );
    }
}
