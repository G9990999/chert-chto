package ru.mws.wiki.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation conflicts with the current resource state,
 * e.g. duplicate username or email.
 */
public class ConflictException extends WikiException {

    /**
     * Constructs a ConflictException with the given message.
     *
     * @param message descriptive conflict message
     */
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
