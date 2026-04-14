package ru.mws.wiki.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all application-specific errors.
 */
public class WikiException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Constructs a WikiException with the given message and HTTP status.
     *
     * @param message error message
     * @param status  HTTP status code to return
     */
    public WikiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return HTTP status
     */
    public HttpStatus getStatus() {
        return status;
    }
}
