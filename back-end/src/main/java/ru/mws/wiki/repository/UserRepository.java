package ru.mws.wiki.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mws.wiki.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their username.
     *
     * @param username the username to look up
     * @return an Optional containing the user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a username is already taken.
     *
     * @param username the username to check
     * @return true if the username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email is already registered.
     *
     * @param email the email to check
     * @return true if the email exists
     */
    boolean existsByEmail(String email);
}
