package ru.mws.wiki.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User entity representing an authenticated user in the system.
 * Audited by Hibernate Envers for change tracking.
 */
@Entity
@Table(name = "users")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** Primary key — UUID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Unique username for login */
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** Unique email address */
    @Column(nullable = false, unique = true, length = 128)
    private String email;

    /** BCrypt-hashed password */
    @Column(nullable = false)
    private String passwordHash;

    /** Display name shown in the UI */
    @Column(length = 128)
    private String displayName;

    /** User role for access control */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    /** Timestamp when account was created */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Pages created by this user */
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Page> ownedPages = new HashSet<>();

    /** Pages shared with this user */
    @ManyToMany(mappedBy = "sharedWith", fetch = FetchType.LAZY)
    private Set<Page> sharedPages = new HashSet<>();

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (role == null) {
            role = Role.USER;
        }
    }
}
