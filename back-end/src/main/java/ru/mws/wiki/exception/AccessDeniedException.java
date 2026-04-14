package ru.mws.wiki.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user does not have permission to access a resource.
 */
public class AccessDeniedException extends WikiException {

    /**
     * Constructs an AccessDeniedException with the given message.
     *
     * @param message descriptive message about what access was denied
     */
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
