package com.lucidity.deliveryrouteoptimizer.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardised error response body. Immutable.
 *
 * @author Rithik Jain
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> messages
) {
    /**
     * Convenience constructor - sets timestamp to now.
     *
     * @param status   HTTP status code
     * @param error    error category
     * @param messages detail messages
     */
    public ErrorResponse(int status, String error, List<String> messages) {
        this(LocalDateTime.now(), status, error, messages);
    }
}