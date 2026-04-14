package ru.mws.wiki.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler that converts application exceptions into
 * RFC 7807 Problem Detail responses.
 *
 * <p>All exceptions are logged at appropriate levels for observability.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all {@link WikiException} subclasses.
     *
     * @param ex the exception
     * @return ProblemDetail with the exception's HTTP status and message
     */
    @ExceptionHandler(WikiException.class)
    public ProblemDetail handleWikiException(WikiException ex) {
        log.warn("Application error [{}]: {}", ex.getStatus(), ex.getMessage());
        return ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
    }

    /**
     * Handles Bean Validation failures from {@code @Valid} annotations.
     *
     * @param ex the validation exception
     * @return ProblemDetail with field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "invalid",
                        (a, b) -> a
                ));
        log.warn("Validation failed: {}", errors);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");
        detail.setProperty("errors", errors);
        return detail;
    }

    /**
     * Fallback handler for unexpected exceptions.
     *
     * @param ex the exception
     * @return ProblemDetail with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
