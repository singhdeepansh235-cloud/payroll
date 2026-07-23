package com.srmcem.payroll.exception;

import com.srmcem.payroll.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralised exception handling for all REST controllers.
 *
 * <p>Catches exceptions thrown anywhere in the application and converts them
 * into a consistent {@link ApiResponse} JSON body so the client never receives
 * a raw Spring error page or an inconsistent error shape.
 *
 * <p>Handler resolution order (most-specific wins):
 * <ol>
 *   <li>{@link ResourceNotFoundException}  → 404</li>
 *   <li>{@link BadRequestException}        → 400</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 (Bean Validation failures)</li>
 *   <li>{@link Exception}                  → 500 (catch-all)</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -----------------------------------------------------------------------
    // 404 – Resource not found
    // -----------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // -----------------------------------------------------------------------
    // 400 – Bad request (business rule violation)
    // -----------------------------------------------------------------------

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(
            BadRequestException ex) {

        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // -----------------------------------------------------------------------
    // 400 – Bean Validation failures (@Valid / @Validated)
    // -----------------------------------------------------------------------

    /**
     * Collects all field-level validation errors into a map and returns them
     * as the {@code data} field so the client knows exactly which fields failed.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
          .getAllErrors()
          .forEach(error -> {
              String field   = ((FieldError) error).getField();
              String message = error.getDefaultMessage();
              errors.put(field, message);
          });

        log.warn("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed.", errors));
    }

    // -----------------------------------------------------------------------
    // 500 – Catch-all for unexpected errors
    // -----------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}
