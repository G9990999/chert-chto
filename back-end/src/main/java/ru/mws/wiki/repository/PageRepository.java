package ru.mws.wiki.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mws.wiki.entity.Page;
import ru.mws.wiki.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Page} entity.
 */
@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {

    /**
     * Find all pages visible to a given user (own + shared + public).
     *
     * @param user the requesting user
     * @return list of accessible pages
     */
    @Query("""
            SELECT DISTINCT p FROM Page p
            LEFT JOIN p.sharedWith sw
            WHERE p.author = :user
               OR sw = :user
               OR p.publicPage = true
            ORDER BY p.updatedAt DESC
            """)
    List<Page> findAccessibleByUser(@Param("user") User user);

    /**
     * Find pages that contain a backlink to a given page (incoming links).
     *
     * @param targetId UUID of the target page
     * @return list of pages linking to the target
     */
    @Query("""
            SELECT p FROM Page p
            WHERE :targetId MEMBER OF p.linkedPageIds
            """)
    List<Page> findByLinkedPageIdsContaining(@Param("targetId") UUID targetId);

    /**
     * Full-text search on title (case-insensitive).
     *
     * @param query search term
     * @return matching pages
     */
    List<Page> findByTitleContainingIgnoreCase(String query);
}
