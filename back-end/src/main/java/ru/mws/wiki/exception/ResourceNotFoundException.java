package ru.mws.wiki.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource (page, user, etc.) is not found.
 */
public class ResourceNotFoundException extends WikiException {

    /**
     * Constructs a ResourceNotFoundException for a given resource type and ID.
     *
     * @param resource resource type name (e.g. "Page")
     * @param id       the identifier that was not found
     */
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id, HttpStatus.NOT_FOUND);
    }
}
