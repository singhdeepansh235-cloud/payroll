package com.srmcem.payroll.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource cannot be found in the database.
 *
 * <p>Maps to HTTP {@code 404 Not Found} via {@link ResponseStatus}.
 * The {@link GlobalExceptionHandler} also catches this explicitly so it
 * can wrap the message inside the standard {@code ApiResponse} envelope.
 *
 * <p>Usage:
 * <pre>
 *   Employee emp = employeeRepository.findById(id)
 *       .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
 * </pre>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
