package ru.mws.wiki.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Wiki page entity. Stores rich-text content (TipTap JSON) and metadata.
 *
 * <p>Hibernate Envers tracks all revisions. The {@code version} field enables
 * optimistic locking to prevent concurrent-write conflicts.</p>
 */
@Entity
@Table(name = "pages")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {

    /** Primary key — UUID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Human-readable page title */
    @Column(nullable = false, length = 255)
    private String title;

    /** TipTap JSON document content stored as TEXT */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** User who created the page */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Creation timestamp */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Last modification timestamp */
    @Column(nullable = false)
    private Instant updatedAt;

    /** Optimistic locking version */
    @Version
    private Long version;

    /** Whether the page is publicly visible to all authenticated users */
    @Column(nullable = false)
    private boolean publicPage;

    /** Users this page is shared with (in addition to the owner) */
    @ManyToMany
    @JoinTable(
            name = "page_shares",
            joinColumns = @JoinColumn(name = "page_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> sharedWith = new HashSet<>();

    /**
     * Outgoing backlinks — pages that this page references.
     * Stored as UUIDs to avoid circular FK issues.
     */
    @ElementCollection
    @CollectionTable(name = "page_links", joinColumns = @JoinColumn(name = "source_page_id"))
    @Column(name = "target_page_id")
    @Builder.Default
    private Set<UUID> linkedPageIds = new HashSet<>();

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
