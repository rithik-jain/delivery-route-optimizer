package com.lucidity.deliveryrouteoptimizer.exception;

/**
 * Thrown when a delivery request fails business-level validation
 * that goes beyond simple field-level checks.
 *
 * @author Rithik Jain
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
