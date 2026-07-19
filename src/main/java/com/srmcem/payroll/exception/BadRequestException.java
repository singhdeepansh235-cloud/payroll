package com.srmcem.payroll.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the caller supplies invalid or semantically incorrect input.
 *
 * <p>Maps to HTTP {@code 400 Bad Request}. Use this for business-rule violations
 * that cannot be caught by Bean Validation alone — for example, trying to process
 * payroll for a month that has already been processed.
 *
 * <p>Usage:
 * <pre>
 *   if (payrollAlreadyProcessed) {
 *       throw new BadRequestException("Payroll for " + month + " has already been processed.");
 *   }
 * </pre>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
